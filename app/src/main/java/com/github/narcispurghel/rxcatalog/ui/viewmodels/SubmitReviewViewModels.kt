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
import com.github.narcispurghel.rxcatalog.catalog.SubmissionDetailsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.Uuid

data class SubmissionMedicineOption(
	val medicineId: String,
	val title: String,
	val supportingText: String,
)

private data class SubmissionDraftSelection(
	val submission: SubmissionDetailsItem?,
	val availableMedicines: List<SubmissionMedicineOption>,
	val medicineId: String,
)

private data class SubmissionDraftContent(
	val selection: SubmissionDraftSelection,
	val title: String,
	val content: String,
)

data class SubmitLeafletUiState(
	val isLoading: Boolean = true,
	val errorMessage: String? = null,
	val submission: SubmissionDetailsItem? = null,
	val medicineId: String = "",
	val selectedMedicineLabel: String? = null,
	val availableMedicines: List<SubmissionMedicineOption> = emptyList(),
	val canEditMedicineId: Boolean = true,
	val title: String = "",
	val content: String = "",
	val isSaving: Boolean = false,
)

@HiltViewModel
class SubmitLeafletViewModel
	@Inject
	constructor(
		savedStateHandle: SavedStateHandle,
		authRepository: AuthRepository,
		private val catalogRepository: CatalogRepository,
	) : ViewModel() {
		private val submissionIdState =
			MutableStateFlow(
				savedStateHandle
					.get<String>("submissionId")
					.orEmpty()
					.takeIf { it.isNotBlank() }
					?.toUuidOrNull(),
			)
		private val medicineIdState =
			MutableStateFlow(savedStateHandle.get<String>("medicineId").orEmpty())
		private val titleState = MutableStateFlow("")
		private val contentState = MutableStateFlow("")
		private val isSavingState = MutableStateFlow(false)
		private val errorMessageState = MutableStateFlow<String?>(null)
		private val draftHydratedState = MutableStateFlow(false)
		private val currentUserIdState = MutableStateFlow<Uuid?>(null)

		private val submissionFlow =
			submissionIdState.flatMapLatest { submissionId ->
				if (submissionId == null) {
					flowOf(null)
				} else {
					catalogRepository.observeSubmissionDetails(submissionId)
				}
			}
		private val availableMedicinesFlow =
			catalogRepository
				.observeMedicines("")
				.map { medicines -> medicines.map { it.toSubmissionMedicineOption() } }

		init {
			authRepository
				.observeCurrentUser()
				.onEach { currentUserIdState.value = it?.userId }
				.launchIn(viewModelScope)

			submissionFlow
				.onEach { submission ->
					if (submission != null && !draftHydratedState.value) {
						medicineIdState.value = submission.medicineId
						titleState.value = submission.title
						contentState.value = submission.content
						draftHydratedState.value = true
					}
				}.launchIn(viewModelScope)
		}

		private val draftUiState =
			combine(submissionFlow, availableMedicinesFlow) { submission, availableMedicines ->
				submission to availableMedicines
			}.combine(
				medicineIdState,
			) { (submission, availableMedicines), medicineId ->
				SubmissionDraftSelection(
					submission = submission,
					availableMedicines = availableMedicines,
					medicineId = medicineId,
				)
			}.combine(
				titleState,
			) { selection, title ->
				SubmissionDraftContent(
					selection = selection,
					title = title,
					content = "",
				)
			}.combine(
				contentState,
			) { draftContent, content ->
				draftContent.copy(content = content)
			}.combine(
				isSavingState,
			) { draftContent, saving ->
				val submission = draftContent.selection.submission
				val availableMedicines = draftContent.selection.availableMedicines
				val medicineId = draftContent.selection.medicineId
				val title = draftContent.title
				val content = draftContent.content
				val selectedMedicineLabel =
					submission?.medicineName
						?: availableMedicines.firstOrNull { it.medicineId == medicineId }?.title
				SubmitLeafletUiState(
					isLoading = false,
					submission = submission,
					medicineId = medicineId,
					selectedMedicineLabel = selectedMedicineLabel,
					availableMedicines = availableMedicines,
					canEditMedicineId = submission == null,
					title = title,
					content = content,
					isSaving = saving,
				)
			}

		val uiState: StateFlow<SubmitLeafletUiState> =
			combine(draftUiState, errorMessageState) { state, error ->
				state.copy(errorMessage = error)
			}.onStart { emit(SubmitLeafletUiState()) }
				.catch {
					emit(
						SubmitLeafletUiState(
							isLoading = false,
							errorMessage = "Unable to load the submission form.",
						),
					)
				}.stateIn(
					scope = viewModelScope,
					started = SharingStarted.WhileSubscribed(5_000),
					initialValue = SubmitLeafletUiState(),
				)

		fun onMedicineIdChanged(value: String) {
			errorMessageState.value = null
			medicineIdState.value = value
		}

		fun onMedicineSelected(medicineId: String) {
			errorMessageState.value = null
			medicineIdState.value = medicineId
		}

		fun onTitleChanged(value: String) {
			errorMessageState.value = null
			titleState.value = value
		}

		fun onContentChanged(value: String) {
			errorMessageState.value = null
			contentState.value = value
		}

		fun saveDraft() {
			viewModelScope.launch {
				isSavingState.value = true
				errorMessageState.value = null
				try {
					if (isReadOnlySubmission()) {
						errorMessageState.value = "Approved submissions are read-only."
						return@launch
					}

					runCatching { persistDraft() }
						.onFailure {
							errorMessageState.value = "Unable to save the draft."
						}
				} finally {
					isSavingState.value = false
				}
			}
		}

		fun submitForReview() {
			viewModelScope.launch {
				isSavingState.value = true
				errorMessageState.value = null
				try {
					if (isReadOnlySubmission()) {
						errorMessageState.value = "Approved submissions are read-only."
						return@launch
					}

					val savedSubmissionId =
						runCatching { persistDraft() }.getOrElse {
							errorMessageState.value = "Unable to save the draft."
							null
						}
					if (savedSubmissionId != null) {
						runCatching {
							catalogRepository.submitForReview(savedSubmissionId)
						}.onFailure {
							errorMessageState.value = "Unable to submit the leaflet for review."
						}
					}
				} finally {
					isSavingState.value = false
				}
			}
		}

		private suspend fun persistDraft(): Uuid? {
			if (isReadOnlySubmission()) {
				return submissionIdState.value
			}
			val userId =
				currentUserIdState.value ?: run {
					errorMessageState.value = "Sign in to continue."
					return null
				}
			val medicineId =
				resolveMedicineId() ?: run {
					errorMessageState.value = "Choose a medicine before saving or submitting."
					return null
				}

			val savedSubmissionId =
				catalogRepository.saveSubmissionDraft(
					submissionId = submissionIdState.value,
					medicineId = medicineId,
					submittedByUserId = userId,
					title = titleState.value,
					content = contentState.value,
				)
			submissionIdState.value = savedSubmissionId
			draftHydratedState.value = true
			return savedSubmissionId
		}

		private fun isReadOnlySubmission(): Boolean =
			uiState.value.submission
				?.statusLabel
				.equals("Approved", ignoreCase = true)

		private fun resolveMedicineId(): Uuid? =
			runCatching { Uuid.parse(medicineIdState.value.trim()) }.getOrNull()

		private fun String.toUuidOrNull(): Uuid? = runCatching { Uuid.parse(this) }.getOrNull()

		private fun MedicineListItem.toSubmissionMedicineOption(): SubmissionMedicineOption {
			val details =
				listOfNotNull(
					brandName?.takeIf { it.isNotBlank() },
					activeIngredient?.takeIf { it.isNotBlank() },
					atcCode?.takeIf { it.isNotBlank() }?.let { "ATC $it" },
				)

			return SubmissionMedicineOption(
				medicineId = medicineId,
				title = canonicalName,
				supportingText = details.joinToString(" • ").ifBlank { "Medicine record" },
			)
		}
	}

data class ReviewSubmissionUiState(
	val isLoading: Boolean = true,
	val errorMessage: String? = null,
	val submission: SubmissionDetailsItem? = null,
	val reviewerNote: String = "",
	val isSaving: Boolean = false,
)

@HiltViewModel
class ReviewSubmissionViewModel
	@Inject
	constructor(
		savedStateHandle: SavedStateHandle,
		authRepository: AuthRepository,
		private val catalogRepository: CatalogRepository,
	) : ViewModel() {
		private val submissionId =
			savedStateHandle
				.get<String>("submissionId")
				.orEmpty()
				.takeIf { it.isNotBlank() }
				?.toUuidOrNull()
		private val reviewerNoteState = MutableStateFlow("")
		private val isSavingState = MutableStateFlow(false)
		private val errorMessageState = MutableStateFlow<String?>(null)
		private val currentUserIdState = MutableStateFlow<Uuid?>(null)

		private val submissionFlow =
			submissionId?.let(catalogRepository::observeSubmissionDetails) ?: flowOf(null)

		init {
			authRepository
				.observeCurrentUser()
				.onEach { currentUserIdState.value = it?.userId }
				.launchIn(viewModelScope)
		}

		val uiState: StateFlow<ReviewSubmissionUiState> =
			combine(
				submissionFlow,
				reviewerNoteState,
				isSavingState,
				errorMessageState,
			) { submission, reviewerNote, saving, error ->
				ReviewSubmissionUiState(
					isLoading = false,
					errorMessage = error,
					submission = submission,
					reviewerNote = reviewerNote,
					isSaving = saving,
				)
			}.onStart {
				emit(
					ReviewSubmissionUiState(
						isLoading = submissionId != null,
					),
				)
			}.catch {
				emit(
					ReviewSubmissionUiState(
						isLoading = false,
						errorMessage = "Unable to load the review.",
					),
				)
			}.stateIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(5_000),
				initialValue = ReviewSubmissionUiState(),
			)

		fun onReviewerNoteChanged(value: String) {
			errorMessageState.value = null
			reviewerNoteState.value = value
		}

		fun approve() = review(approve = true)

		fun reject() = review(approve = false)

		private fun review(approve: Boolean) {
			val reviewerId =
				currentUserIdState.value ?: run {
					errorMessageState.value = "Sign in to continue."
					return
				}
			val currentSubmissionId =
				submissionId ?: run {
					errorMessageState.value = "Unable to open the review."
					return
				}
			if (uiState.value.submission == null) {
				errorMessageState.value = "Submission details are unavailable."
				return
			}

			viewModelScope.launch {
				isSavingState.value = true
				errorMessageState.value = null
				runCatching {
					catalogRepository.reviewSubmission(
						submissionId = currentSubmissionId,
						reviewerUserId = reviewerId,
						approve = approve,
						notes = reviewerNoteState.value.takeIf { it.isNotBlank() },
					)
				}.onFailure {
					errorMessageState.value = "Unable to record the review."
				}
				isSavingState.value = false
			}
		}

		private fun String.toUuidOrNull(): Uuid? = runCatching { Uuid.parse(this) }.getOrNull()
	}
