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
import com.github.narcispurghel.rxcatalog.catalog.LeafletDetailsItem
import com.github.narcispurghel.rxcatalog.catalog.MedicineListItem
import com.github.narcispurghel.rxcatalog.catalog.MedicineDetailsItem
import com.github.narcispurghel.rxcatalog.catalog.PendingApprovalListItem
import com.github.narcispurghel.rxcatalog.catalog.SubmissionListItem
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.ApprovalQueueItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.Uuid

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

    init {
        query
            .debounce(250)
            .map { it.trim() }
            .onEach { trimmedQuery ->
                runCatching {
                    catalogRepository.refreshMedicines(trimmedQuery)
                }
            }
            .launchIn(viewModelScope)
    }

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

data class MedicineDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val medicine: MedicineDetailsItem? = null,
)

@HiltViewModel
class MedicineDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val medicineId =
        runCatching {
            Uuid.parse(savedStateHandle.get<String>("medicineId").orEmpty())
        }.getOrNull()
    private val refreshCompleted = MutableStateFlow(false)

    private val detailsFlow: Flow<MedicineDetailsUiState> =
        if (medicineId == null) {
            flowOf(
                MedicineDetailsUiState(
                    isLoading = false,
                    errorMessage = "Unable to open that medicine.",
                ),
                )
        } else {
            catalogRepository.observeMedicineDetails(medicineId)
                .combine(refreshCompleted) { medicine, refreshDone ->
                    MedicineDetailsUiState(
                        isLoading = medicine == null && !refreshDone,
                        medicine = medicine,
                        errorMessage =
                            if (medicine == null && refreshDone) {
                                "Medicine details are unavailable."
                            } else {
                                null
                            },
                    )
                }
                .onStart {
                    emit(MedicineDetailsUiState())
                    viewModelScope.launch {
                        runCatching {
                            catalogRepository.refreshMedicineDetails(medicineId)
                        }.also {
                            refreshCompleted.value = true
                        }
                    }
                }
                .catch {
                    emit(
                        MedicineDetailsUiState(
                            isLoading = false,
                            errorMessage = "Unable to load medicine details.",
                        ),
                    )
                }
        }

    val uiState: StateFlow<MedicineDetailsUiState> =
        detailsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicineDetailsUiState(),
        )
}

data class LeafletDetailsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val leaflet: LeafletDetailsItem? = null,
)

@HiltViewModel
class LeafletDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val leafletId =
        runCatching {
            Uuid.parse(savedStateHandle.get<String>("leafletId").orEmpty())
        }.getOrNull()
    private val refreshCompleted = MutableStateFlow(false)

    private val detailsFlow: Flow<LeafletDetailsUiState> =
        if (leafletId == null) {
            flowOf(
                LeafletDetailsUiState(
                    isLoading = false,
                    errorMessage = "Unable to open that leaflet.",
                ),
                )
        } else {
            catalogRepository.observeLeafletDetails(leafletId)
                .combine(refreshCompleted) { leaflet, refreshDone ->
                    LeafletDetailsUiState(
                        isLoading = leaflet == null && !refreshDone,
                        leaflet = leaflet,
                        errorMessage =
                            if (leaflet == null && refreshDone) {
                                "Approved leaflet details are unavailable."
                            } else {
                                null
                            },
                    )
                }
                .onStart {
                    emit(LeafletDetailsUiState())
                    viewModelScope.launch {
                        runCatching {
                            catalogRepository.refreshLeafletDetails(leafletId)
                        }.also {
                            refreshCompleted.value = true
                        }
                    }
                }
                .catch {
                    emit(
                        LeafletDetailsUiState(
                            isLoading = false,
                            errorMessage = "Unable to load leaflet details.",
                        ),
                    )
                }
        }

    val uiState: StateFlow<LeafletDetailsUiState> =
        detailsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LeafletDetailsUiState(),
        )
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
