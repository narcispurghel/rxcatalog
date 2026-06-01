package com.github.narcispurghel.rxcatalog.ui.session

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import com.github.narcispurghel.rxcatalog.common.UserRole

@Stable
class DemoSessionState(
    initialAuthenticated: Boolean,
    initialRole: UserRole?,
) {
    var isAuthenticated by mutableStateOf(initialAuthenticated)
        private set

    var role by mutableStateOf(initialRole)
        private set

    fun signIn(role: UserRole) {
        isAuthenticated = true
        this.role = role
    }

    fun signOut() {
        isAuthenticated = false
        role = null
    }

    companion object {
        val Saver =
            mapSaver<DemoSessionState>(
                save = {
                    mapOf(
                        "authenticated" to it.isAuthenticated,
                        "role" to it.role?.name,
                    )
                },
                restore = { restored ->
                    DemoSessionState(
                        initialAuthenticated = restored["authenticated"] as? Boolean ?: false,
                        initialRole = (restored["role"] as? String)?.let(UserRole::valueOf),
                    )
                },
            )
    }
}
