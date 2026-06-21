@file:OptIn(
	kotlin.uuid.ExperimentalUuidApi::class,
	kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)

package com.github.narcispurghel.rxcatalog.auth

import android.database.sqlite.SQLiteConstraintException
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.persistence.SyncStatus
import com.github.narcispurghel.rxcatalog.persistence.UserDao
import com.github.narcispurghel.rxcatalog.persistence.UserEntity
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class LocalAuthRepository
	@Inject
	constructor(
		private val userDao: UserDao,
		private val sessionDataStore: SessionDataStore,
		private val passwordHasher: PasswordHasher,
	) : AuthRepository {
		override fun observeSession(): Flow<SessionState> =
			sessionDataStore
				.observeSessionPreferences()
				.distinctUntilChanged()
				.flatMapLatest { preferences ->
					val userId = preferences.currentUserId
					if (!preferences.isAuthenticated || userId == null) {
						flowOf(SessionState.Unauthenticated)
					} else {
						userDao.observeActiveById(userId).map { entity ->
							if (entity == null) {
								SessionState.Unauthenticated
							} else {
								SessionState.Authenticated(entity.toAuthenticatedUser())
							}
						}
					}
				}.onStart {
					emit(SessionState.Loading)
				}.distinctUntilChanged()

		override fun observeCurrentUser(): Flow<AuthenticatedUser?> =
			observeSession()
				.map { state ->
					if (state is SessionState.Authenticated) {
						state.user
					} else {
						null
					}
				}.distinctUntilChanged()

		override suspend fun register(
			email: String,
			password: String,
			displayName: String,
			role: UserRole,
		): AuthResult {
			val normalizedEmail = normalizeEmail(email)
			val trimmedDisplayName = displayName.trim()
			val trimmedPassword = password.trim()
			if (normalizedEmail.isEmpty() || trimmedDisplayName.isEmpty() ||
				trimmedPassword.isEmpty()
			) {
				return AuthResult.Failure(AuthError.InvalidCredentials)
			}

			val now = System.currentTimeMillis()
			val existingUser = userDao.getByNormalizedEmail(normalizedEmail)
			if (existingUser != null) {
				return AuthResult.Failure(AuthError.EmailAlreadyRegistered)
			}

			val user =
				UserEntity(
					userId = Uuid.random(),
					email = normalizedEmail,
					displayName = trimmedDisplayName,
					passwordHash = passwordHasher.hash(trimmedPassword),
					role = role.toPersistenceRole(),
					createdAt = now,
					updatedAt = now,
					lastLoginAt = now,
					syncStatus = SyncStatus.PENDING_CREATE,
				)
			try {
				userDao.insert(user)
			} catch (exception: SQLiteConstraintException) {
				return AuthResult.Failure(AuthError.EmailAlreadyRegistered)
			}
			sessionDataStore.setSession(user.userId, role)

			return AuthResult.Success(user.toAuthenticatedUser())
		}

		override suspend fun login(
			email: String,
			password: String,
		): AuthResult {
			val normalizedEmail = normalizeEmail(email)
			val trimmedPassword = password.trim()
			if (normalizedEmail.isEmpty() || trimmedPassword.isEmpty()) {
				return AuthResult.Failure(AuthError.InvalidCredentials)
			}

			val now = System.currentTimeMillis()
			val user =
				userDao.getByNormalizedEmail(normalizedEmail)
					?: return AuthResult.Failure(AuthError.InvalidCredentials)
			if (!user.isActive) {
				return AuthResult.Failure(AuthError.InactiveAccount)
			}
			if (!passwordHasher.verify(trimmedPassword, user.passwordHash)) {
				return AuthResult.Failure(AuthError.InvalidCredentials)
			}

			userDao.updateLastLogin(
				userId = user.userId,
				lastLoginAt = now,
				updatedAt = now,
				syncStatus = SyncStatus.PENDING_UPDATE,
			)
			sessionDataStore.setSession(user.userId, user.role.toCommonUserRole())

			return AuthResult.Success(
				user
					.copy(
						lastLoginAt = now,
						updatedAt = now,
						syncStatus = SyncStatus.PENDING_UPDATE,
					).toAuthenticatedUser(),
			)
		}

		override suspend fun logout() {
			sessionDataStore.clearSession()
		}

		private fun UserEntity.toAuthenticatedUser(): AuthenticatedUser =
			AuthenticatedUser(
				userId = userId,
				email = email,
				displayName = displayName,
				role = role.toCommonUserRole(),
			)

		private fun normalizeEmail(email: String): String = email.trim().lowercase()
	}
