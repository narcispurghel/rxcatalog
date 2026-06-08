@file:OptIn(
    kotlin.uuid.ExperimentalUuidApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)

package com.github.narcispurghel.rxcatalog.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.narcispurghel.rxcatalog.auth.AuthRepository
import com.github.narcispurghel.rxcatalog.catalog.CatalogRepository
import com.github.narcispurghel.rxcatalog.catalog.MedicineListItem
import com.github.narcispurghel.rxcatalog.catalog.PendingApprovalListItem
import com.github.narcispurghel.rxcatalog.catalog.SubmissionListItem
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.ApprovalQueueItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val medicines: List<SearchResultItem> = emptyList(),
)

data class SearchResultItem(
    val medicineId: String,
    val canonicalName: String,
    val brandName: String?,
    val activeIngredient: String?,
    val atcCode: String?,
    val description: String?,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val query = MutableStateFlow(savedStateHandle.get<String>("query").orEmpty())

    val uiState: StateFlow<SearchUiState> =
        query.flatMapLatest { currentQuery ->
            catalogRepository.observeMedicines(currentQuery.trim())
                .map { medicines ->
                    SearchUiState(
                        query = currentQuery,
                        isLoading = false,
                        medicines = medicines.map { it.toSearchResultItem() },
                    )
                }
                .onStart {
                    emit(
                        SearchUiState(
                            query = currentQuery,
                            isLoading = true,
                        ),
                    )
                }
                .catch {
                    emit(
                        SearchUiState(
                            query = currentQuery,
                            isLoading = false,
                            errorMessage = "Unable to load medicines.",
                        ),
                    )
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(query = query.value, isLoading = true),
        )

    fun onQueryChanged(value: String) {
        query.value = value
    }
}

data class MySubmissionsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val submissions: List<MySubmissionItem> = emptyList(),
)

data class MySubmissionItem(
    val submissionId: String,
    val medicineName: String,
    val title: String,
    val statusLabel: String,
    val updatedLabel: String,
    val actionLabel: String,
)

@HiltViewModel
class MySubmissionsViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    val uiState: StateFlow<MySubmissionsUiState> =
        authRepository.observeCurrentUser()
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(MySubmissionsUiState())
                } else {
                    catalogRepository.observeSubmissionsForUser(user.userId)
                        .map { submissions ->
                            MySubmissionsUiState(
                                isLoading = false,
                                submissions = submissions.map { it.toSubmissionItem() },
                            )
                        }
                        .onStart {
                            emit(MySubmissionsUiState())
                        }
                        .catch {
                            emit(
                                MySubmissionsUiState(
                                    isLoading = false,
                                    errorMessage = "Unable to load submissions.",
                                ),
                            )
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MySubmissionsUiState(),
            )
}

data class PendingApprovalsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val queue: List<ApprovalQueueItem> = emptyList(),
)

@HiltViewModel
class PendingApprovalsViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    val uiState: StateFlow<PendingApprovalsUiState> =
        catalogRepository.observePendingApprovals()
            .map { queue ->
                PendingApprovalsUiState(
                    isLoading = false,
                    queue = queue.map { it.toApprovalQueueItem() },
                )
            }
            .onStart {
                emit(PendingApprovalsUiState())
            }
            .catch {
                emit(
                    PendingApprovalsUiState(
                        isLoading = false,
                        errorMessage = "Unable to load pending approvals.",
                    ),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PendingApprovalsUiState(),
            )
}

private fun MedicineListItem.toSearchResultItem(): SearchResultItem =
    SearchResultItem(
        medicineId = medicineId.toString(),
        canonicalName = canonicalName,
        brandName = brandName,
        activeIngredient = activeIngredient,
        atcCode = atcCode,
        description = description,
    )

private fun SubmissionListItem.toSubmissionItem(): MySubmissionItem =
    MySubmissionItem(
        submissionId = submissionId.toString(),
        medicineName = medicineName,
        title = title,
        statusLabel = statusLabel,
        updatedLabel = updatedLabel,
        actionLabel = actionLabel,
    )

private fun PendingApprovalListItem.toApprovalQueueItem(): ApprovalQueueItem =
    ApprovalQueueItem(
        submissionId = submissionId.toString(),
        medicineName = medicineName,
        submittedBy = submittedBy,
        createdAtLabel = createdAtLabel,
        isUrgent = isUrgent,
    )
