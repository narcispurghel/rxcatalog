package com.github.narcispurghel.rxcatalog.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
	val sessionState: SessionState = SessionState.Loading,
	val currentUser: AuthenticatedUser? = null,
	val isLoggingOut: Boolean = false,
	val logoutError: String? = null,
)

@HiltViewModel
class SessionViewModel
	@Inject
	constructor(
		private val authRepository: AuthRepository,
	) : ViewModel() {
		var uiState by mutableStateOf(SessionUiState())
			private set

		init {
			viewModelScope.launch {
				combine(
					authRepository.observeSession(),
					authRepository.observeCurrentUser(),
				) { sessionState, currentUser ->
					SessionUiState(
						sessionState = sessionState,
						currentUser = currentUser,
						isLoggingOut = uiState.isLoggingOut,
						logoutError = uiState.logoutError,
					)
				}.collectLatest { collected ->
					uiState = collected
				}
			}
		}

		fun clearLogoutError() {
			uiState = uiState.copy(logoutError = null)
		}

		fun logout() {
			if (uiState.isLoggingOut) return

			viewModelScope.launch {
				uiState = uiState.copy(isLoggingOut = true, logoutError = null)
				try {
					authRepository.logout()
					uiState = uiState.copy(isLoggingOut = false)
				} catch (exception: CancellationException) {
					throw exception
				} catch (exception: Exception) {
					uiState =
						uiState.copy(
							isLoggingOut = false,
							logoutError = exception.message ?: "Unable to log out.",
						)
				}
			}
		}
	}
