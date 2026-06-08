@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.auth

import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.persistence.SyncStatus
import com.github.narcispurghel.rxcatalog.persistence.UserDao
import com.github.narcispurghel.rxcatalog.persistence.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.Uuid

class LocalAuthRepositoryTest {
    private val passwordHasher = PasswordHasher()

    @Test
    fun `password hasher verifies correct password only`() {
        val encoded = passwordHasher.hash("password123")

        assertTrue(passwordHasher.verify("password123", encoded))
        assertFalse(passwordHasher.verify("wrong-password", encoded))
    }

    @Test
    fun `user role mappings stay symmetric`() {
        UserRole.entries.forEach { role ->
            assertEquals(role, role.toPersistenceRole().toCommonUserRole())
        }
    }

    @Test
    fun `observeSession starts unauthenticated when no session exists`() = runTest {
        val repository = createRepository()

        val session = repository.observeSession().drop(1).first()

        assertEquals(SessionState.Unauthenticated, session)
    }

    @Test
    fun `register stores hashed user and authenticates session`() = runTest {
        val userDao = FakeUserDao()
        val sessionStore = FakeSessionDataStore()
        val repository = createRepository(userDao, sessionStore)

        val result = repository.register("Doctor@Example.com", "password123", "Dr. Who", UserRole.DOCTOR)

        assertTrue(result is AuthResult.Success)
        val storedUser = userDao.getByNormalizedEmail("doctor@example.com")
        assertNotNull(storedUser)
        assertEquals("doctor@example.com", storedUser?.email)
        assertEquals("Dr. Who", storedUser?.displayName)
        assertEquals(com.github.narcispurghel.rxcatalog.persistence.UserRole.DOCTOR, storedUser?.role)
        assertNotEquals("password123", storedUser?.passwordHash)
        assertTrue(passwordHasher.verify("password123", storedUser!!.passwordHash))
        assertEquals(
            SessionPreferences(
                currentUserId = storedUser.userId,
                currentUserRole = UserRole.DOCTOR,
                isAuthenticated = true,
            ),
            sessionStore.observeSessionPreferences().first(),
        )
    }

    @Test
    fun `register rejects duplicate normalized email`() = runTest {
        val userDao = FakeUserDao()
        val sessionStore = FakeSessionDataStore()
        val repository = createRepository(userDao, sessionStore)
        repository.register("test@example.com", "password123", "Tester", UserRole.USER)

        val result = repository.register(" TEST@example.com ", "password123", "Another", UserRole.USER)

        assertEquals(AuthResult.Failure(AuthError.EmailAlreadyRegistered), result)
    }

    @Test
    fun `login accepts valid credentials and updates session`() = runTest {
        val userDao = FakeUserDao()
        val sessionStore = FakeSessionDataStore()
        val repository = createRepository(userDao, sessionStore)
        val existingUser = buildUser(email = "pharmacist@example.com", role = UserRole.PHARMACIST)
        userDao.insert(existingUser)

        val result = repository.login("PHARMACIST@example.com", "password123")

        assertTrue(result is AuthResult.Success)
        val updatedUser = userDao.getById(existingUser.userId)
        assertNotNull(updatedUser?.lastLoginAt)
        assertEquals(
            SessionPreferences(
                currentUserId = existingUser.userId,
                currentUserRole = UserRole.PHARMACIST,
                isAuthenticated = true,
            ),
            sessionStore.observeSessionPreferences().first(),
        )
    }

    @Test
    fun `login rejects invalid credentials`() = runTest {
        val userDao = FakeUserDao()
        val repository = createRepository(userDao, FakeSessionDataStore())
        userDao.insert(buildUser(email = "user@example.com"))

        val result = repository.login("user@example.com", "bad-password")

        assertEquals(AuthResult.Failure(AuthError.InvalidCredentials), result)
    }

    @Test
    fun `logout clears persisted session`() = runTest {
        val sessionStore = FakeSessionDataStore()
        val repository = createRepository(FakeUserDao(), sessionStore)
        sessionStore.setSession(Uuid.random(), UserRole.USER)

        repository.logout()

        assertEquals(SessionPreferences(), sessionStore.observeSessionPreferences().first())
    }

    private fun createRepository(
        userDao: FakeUserDao = FakeUserDao(),
        sessionDataStore: FakeSessionDataStore = FakeSessionDataStore(),
    ): LocalAuthRepository =
        LocalAuthRepository(
            userDao = userDao,
            sessionDataStore = sessionDataStore,
            passwordHasher = passwordHasher,
        )

    private fun buildUser(
        email: String,
        role: UserRole = UserRole.USER,
        password: String = "password123",
    ): UserEntity {
        val now = System.currentTimeMillis()
        return UserEntity(
            userId = Uuid.random(),
            email = email,
            displayName = "Existing User",
            passwordHash = passwordHasher.hash(password),
            role = role.toPersistenceRole(),
            createdAt = now,
            updatedAt = now,
            lastLoginAt = null,
            syncStatus = SyncStatus.SYNCED,
        )
    }
}

private class FakeSessionDataStore : SessionDataStore {
    private val state = MutableStateFlow(SessionPreferences())

    override fun observeSessionPreferences(): Flow<SessionPreferences> = state

    override suspend fun setSession(userId: Uuid, role: UserRole) {
        state.value =
            SessionPreferences(
                currentUserId = userId,
                currentUserRole = role,
                isAuthenticated = true,
            )
    }

    override suspend fun clearSession() {
        state.value = SessionPreferences()
    }
}

private class FakeUserDao : UserDao {
    private val users = MutableStateFlow<Map<Uuid, UserEntity>>(emptyMap())

    override suspend fun insert(user: UserEntity) {
        users.value = users.value + (user.userId to user)
    }

    override suspend fun upsert(user: UserEntity) = insert(user)

    override suspend fun upsertAll(users: List<UserEntity>) {
        this.users.value = this.users.value + users.associateBy { it.userId }
    }

    override suspend fun getById(userId: Uuid): UserEntity? = users.value[userId]

    override suspend fun getActiveById(userId: Uuid): UserEntity? = users.value[userId]?.takeIf { it.isActive }

    override fun observeById(userId: Uuid): Flow<UserEntity?> = users.map { it[userId] }

    override fun observeActiveById(userId: Uuid): Flow<UserEntity?> =
        users.map { map -> map[userId]?.takeIf { it.isActive } }

    override suspend fun getByNormalizedEmail(normalizedEmail: String): UserEntity? =
        users.value.values.firstOrNull { it.email.trim().lowercase() == normalizedEmail }

    override fun observeByNormalizedEmail(normalizedEmail: String): Flow<UserEntity?> =
        users.map { map ->
            map.values.firstOrNull { it.email.trim().lowercase() == normalizedEmail }
        }

    override fun observeAll(): Flow<List<UserEntity>> = users.map { it.values.toList() }

    override fun observeByRole(role: com.github.narcispurghel.rxcatalog.persistence.UserRole): Flow<List<UserEntity>> =
        users.map { map -> map.values.filter { it.role == role } }

    override fun observeActiveUsers(): Flow<List<UserEntity>> =
        users.map { map -> map.values.filter { it.isActive } }

    override fun observeBySyncStatus(syncStatus: SyncStatus): Flow<List<UserEntity>> =
        users.map { map -> map.values.filter { it.syncStatus == syncStatus } }

    override fun search(query: String): Flow<List<UserEntity>> =
        users.map { map ->
            map.values.filter {
                it.email.contains(query, ignoreCase = true) ||
                    it.displayName.contains(query, ignoreCase = true)
            }
        }

    override suspend fun updateActiveState(
        userId: Uuid,
        isActive: Boolean,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) {
        val user = users.value[userId] ?: return
        insert(user.copy(isActive = isActive, updatedAt = updatedAt, syncStatus = syncStatus))
    }

    override suspend fun updateLastLogin(
        userId: Uuid,
        lastLoginAt: Long,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) {
        val user = users.value[userId] ?: return
        insert(
            user.copy(
                lastLoginAt = lastLoginAt,
                updatedAt = updatedAt,
                syncStatus = syncStatus,
            ),
        )
    }

    override suspend fun updateRole(
        userId: Uuid,
        role: com.github.narcispurghel.rxcatalog.persistence.UserRole,
        updatedAt: Long,
        syncStatus: SyncStatus,
    ) {
        val user = users.value[userId] ?: return
        insert(user.copy(role = role, updatedAt = updatedAt, syncStatus = syncStatus))
    }

    override suspend fun delete(user: UserEntity) {
        users.value = users.value - user.userId
    }
}
