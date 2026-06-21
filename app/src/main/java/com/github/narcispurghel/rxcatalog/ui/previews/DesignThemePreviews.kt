@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.github.narcispurghel.rxcatalog.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.github.narcispurghel.rxcatalog.auth.AuthFormState
import com.github.narcispurghel.rxcatalog.auth.AuthMode
import com.github.narcispurghel.rxcatalog.auth.AuthenticatedUser
import com.github.narcispurghel.rxcatalog.auth.SessionState
import com.github.narcispurghel.rxcatalog.catalog.ApprovedLeafletItem
import com.github.narcispurghel.rxcatalog.catalog.CatalogSeedIds
import com.github.narcispurghel.rxcatalog.catalog.LeafletDetailsItem
import com.github.narcispurghel.rxcatalog.catalog.MedicineDetailsItem
import com.github.narcispurghel.rxcatalog.common.UserRole
import com.github.narcispurghel.rxcatalog.ui.components.pendingapprovals.sampleApprovalQueue
import com.github.narcispurghel.rxcatalog.ui.screens.*
import com.github.narcispurghel.rxcatalog.ui.theme.RxCatalogTheme
import com.github.narcispurghel.rxcatalog.ui.viewmodels.*
import kotlin.uuid.Uuid

private const val PreviewWidthDp = 411
private const val PreviewHeightDp = 960

private val previewUser =
	AuthenticatedUser(
		userId = Uuid.parse(CatalogSeedIds.DOCTOR_USER_ID),
		email = "doctor.popescu@rxcatalog.test",
		displayName = "Dr. Andrei Popescu",
		role = UserRole.DOCTOR,
	)

private val previewHomeState =
	HomeUiState(
		isLoading = false,
		medicineCount = 4,
		pendingApprovalsCount = 2,
		featuredMedicines =
			listOf(
				HomeFeaturedMedicineItem(
					medicineId = CatalogSeedIds.ASPIRIN_MEDICINE_ID,
					name = "Aspirin",
					detail = "Acetylsalicylic acid",
					hasPendingReview = true,
				),
				HomeFeaturedMedicineItem(
					medicineId = CatalogSeedIds.PARACETAMOL_MEDICINE_ID,
					name = "Paracetamol",
					detail = "Paracetamol",
					hasPendingReview = true,
				),
				HomeFeaturedMedicineItem(
					medicineId = CatalogSeedIds.IBUPROFEN_MEDICINE_ID,
					name = "Ibuprofen",
					detail = "Ibuprofen",
					hasPendingReview = false,
				),
			),
	)

private val previewApprovedLeaflet =
	ApprovedLeafletItem(
		leafletId = "leaflet-aspirin-approved",
		medicineId = CatalogSeedIds.ASPIRIN_MEDICINE_ID,
		title = "Aspirin 100 mg film-coated tablets",
		content =
			"1. What Aspirin is and what it is used for.\n\n" +
				"2. What you need to know before you take Aspirin.\n\n" +
				"3. How to take Aspirin safely.",
		version = 4,
		approvedAtLabel = "Approved 2 days ago",
	)

private val previewMedicine =
	MedicineDetailsItem(
		medicineId = CatalogSeedIds.ASPIRIN_MEDICINE_ID,
		canonicalName = "Aspirin 100 mg",
		brandName = "Bayer Aspirin Protect",
		activeIngredient = "Acetylsalicylic acid",
		atcCode = "B01AC06",
		description = "Antiplatelet therapy used for secondary prevention in cardiovascular care.",
		approvedLeaflet = previewApprovedLeaflet,
	)

private val previewMedicineWithoutLeaflet =
	MedicineDetailsItem(
		medicineId = CatalogSeedIds.PARACETAMOL_MEDICINE_ID,
		canonicalName = "Paracetamol 500 mg",
		brandName = "Panadol",
		activeIngredient = "Paracetamol",
		atcCode = "N02BE01",
		description = "Analgesic and antipyretic record awaiting a newly verified leaflet revision.",
		approvedLeaflet = null,
	)

private val previewLeaflet =
	LeafletDetailsItem(
		leafletId = previewApprovedLeaflet.leafletId,
		medicineName = previewMedicine.canonicalName,
		title = previewApprovedLeaflet.title,
		content = previewApprovedLeaflet.content,
		version = previewApprovedLeaflet.version,
		approvedAtLabel = previewApprovedLeaflet.approvedAtLabel,
	)

private val previewSearchState =
	SearchUiState(
		query = "aspirin",
		isLoading = false,
		medicines =
			listOf(
				SearchResultItem(
					medicineId = CatalogSeedIds.ASPIRIN_MEDICINE_ID,
					canonicalName = "Aspirin 100 mg",
					brandName = "Bayer Aspirin Protect",
					activeIngredient = "Acetylsalicylic acid",
					atcCode = "B01AC06",
					description = "Verified leaflet available for cardiovascular prevention guidance.",
				),
				SearchResultItem(
					medicineId = CatalogSeedIds.PARACETAMOL_MEDICINE_ID,
					canonicalName = "Paracetamol 500 mg",
					brandName = "Panadol",
					activeIngredient = "Paracetamol",
					atcCode = "N02BE01",
					description = "Leaflet update pending reviewer confirmation.",
				),
			),
	)

private val previewSearchEmptyState =
	SearchUiState(
		query = "ceftriaxone",
		isLoading = false,
		medicines = emptyList(),
	)

private val previewSearchLoadingState =
	SearchUiState(
		query = "aspirin",
		isLoading = true,
		medicines = emptyList(),
	)

private val previewSearchErrorState =
	SearchUiState(
		query = "ibuprofen",
		isLoading = false,
		errorMessage = "Unable to load medicines.",
		medicines = emptyList(),
	)

private val previewSubmissionsState =
	MySubmissionsUiState(
		isLoading = false,
		submissions =
			listOf(
				MySubmissionItem(
					submissionId = CatalogSeedIds.PENDING_ASPIRIN_SUBMISSION_ID,
					medicineName = "Aspirin 100 mg",
					title = "Updated contraindications section",
					statusLabel = "Pending review",
					updatedLabel = "Updated 2 hours ago",
					actionLabel = "View submission",
				),
				MySubmissionItem(
					submissionId = CatalogSeedIds.REJECTED_AMOXICILLIN_SUBMISSION_ID,
					medicineName = "Amoxicillin 500 mg",
					title = "Dosage wording revision",
					statusLabel = "Needs revision",
					updatedLabel = "Updated yesterday",
					actionLabel = "Revise submission",
				),
				MySubmissionItem(
					submissionId = CatalogSeedIds.APPROVED_IBUPROFEN_SUBMISSION_ID,
					medicineName = "Ibuprofen 200 mg",
					title = "Approved warning update",
					statusLabel = "Verified leaflet",
					updatedLabel = "Updated 4 days ago",
					actionLabel = "View approved leaflet",
				),
			),
	)

private val previewSubmissionsEmptyState =
	MySubmissionsUiState(
		isLoading = false,
		submissions = emptyList(),
	)

private val previewSubmissionsLoadingState =
	MySubmissionsUiState(
		isLoading = true,
		submissions = emptyList(),
	)

private val previewSubmissionsErrorState =
	MySubmissionsUiState(
		isLoading = false,
		errorMessage = "Unable to load submissions.",
		submissions = emptyList(),
	)

private val previewPendingApprovalsState =
	PendingApprovalsUiState(
		isLoading = false,
		queue = sampleApprovalQueue(),
	)

private val previewPendingApprovalsEmptyState =
	PendingApprovalsUiState(
		isLoading = false,
		queue = emptyList(),
	)

private val previewPendingApprovalsLoadingState =
	PendingApprovalsUiState(
		isLoading = true,
		queue = emptyList(),
	)

private val previewPendingApprovalsErrorState =
	PendingApprovalsUiState(
		isLoading = false,
		errorMessage = "Unable to load pending approvals.",
		queue = emptyList(),
	)

private val previewAuthState =
	AuthFormState(
		mode = AuthMode.REGISTER,
		displayName = "Dr. Andrei Popescu",
		email = "doctor.popescu@rxcatalog.test",
		password = "Password123",
		confirmPassword = "Password123",
		selectedRole = UserRole.DOCTOR,
	)

private val previewMedicineDetailsState =
	MedicineDetailsUiState(
		isLoading = false,
		medicine = previewMedicine,
	)

private val previewMedicineDetailsWithoutLeafletState =
	MedicineDetailsUiState(
		isLoading = false,
		medicine = previewMedicineWithoutLeaflet,
	)

private val previewLeafletDetailsState =
	LeafletDetailsUiState(
		isLoading = false,
		leaflet = previewLeaflet,
	)

@Composable
private fun PreviewTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	RxCatalogTheme(darkTheme = darkTheme, content = content)
}

@Preview(
	name = "Auth Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Composable
fun AuthScreenLightPreview() {
	PreviewTheme {
		AuthScreen(
			state = previewAuthState,
			onDisplayNameChanged = {},
			onEmailChanged = {},
			onPasswordChanged = {},
			onConfirmPasswordChanged = {},
			onRoleSelected = {},
			onSubmit = {},
			onSwitch = {},
			onDismissError = {},
		)
	}
}

@Preview(
	name = "Auth Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun AuthScreenDarkPreview() {
	PreviewTheme(darkTheme = true) {
		AuthScreen(
			state = previewAuthState,
			onDisplayNameChanged = {},
			onEmailChanged = {},
			onPasswordChanged = {},
			onConfirmPasswordChanged = {},
			onRoleSelected = {},
			onSubmit = {},
			onSwitch = {},
			onDismissError = {},
		)
	}
}

@Preview(
	name = "Home Shell Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Composable
fun HomeScreenLightPreview() {
	PreviewTheme {
		HomeScreen(
			sessionState = SessionState.Authenticated(previewUser),
			currentUser = previewUser,
			homeState = previewHomeState,
			onSearch = {},
			onSubmit = {},
			onApprovals = {},
			onProfile = {},
			onMedicine = {},
		)
	}
}

@Preview(
	name = "Home Shell Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun HomeScreenDarkPreview() {
	PreviewTheme(darkTheme = true) {
		HomeScreen(
			sessionState = SessionState.Authenticated(previewUser),
			currentUser = previewUser,
			homeState = previewHomeState,
			onSearch = {},
			onSubmit = {},
			onApprovals = {},
			onProfile = {},
			onMedicine = {},
		)
	}
}

@Preview(
	name = "Search Results Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Composable
fun SearchScreenResultsLightPreview() {
	PreviewTheme {
		SearchScreen(
			state = previewSearchState,
			onQueryChanged = {},
			onMedicine = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Search Results Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SearchScreenResultsDarkPreview() {
	PreviewTheme(darkTheme = true) {
		SearchScreen(
			state = previewSearchState,
			onQueryChanged = {},
			onMedicine = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Search Empty Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Search Empty Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SearchScreenEmptyPreview() {
	PreviewTheme {
		SearchScreen(
			state = previewSearchEmptyState,
			onQueryChanged = {},
			onMedicine = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Search Loading Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Search Loading Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SearchScreenLoadingPreview() {
	PreviewTheme {
		SearchScreen(
			state = previewSearchLoadingState,
			onQueryChanged = {},
			onMedicine = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Search Error Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Search Error Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SearchScreenErrorPreview() {
	PreviewTheme {
		SearchScreen(
			state = previewSearchErrorState,
			onQueryChanged = {},
			onMedicine = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Medicine Details With Leaflet Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Composable
fun MedicineDetailsScreenWithLeafletLightPreview() {
	PreviewTheme {
		MedicineDetailsScreen(
			state = previewMedicineDetailsState,
			onOpenLeaflet = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Medicine Details With Leaflet Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun MedicineDetailsScreenWithLeafletDarkPreview() {
	PreviewTheme(darkTheme = true) {
		MedicineDetailsScreen(
			state = previewMedicineDetailsState,
			onOpenLeaflet = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Medicine Details No Leaflet Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Medicine Details No Leaflet Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun MedicineDetailsScreenWithoutLeafletPreview() {
	PreviewTheme {
		MedicineDetailsScreen(
			state = previewMedicineDetailsWithoutLeafletState,
			onOpenLeaflet = {},
			onSubmit = {},
		)
	}
}

@Preview(
	name = "Leaflet Details Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Composable
fun LeafletDetailsScreenLightPreview() {
	PreviewTheme {
		LeafletDetailsScreen(state = previewLeafletDetailsState)
	}
}

@Preview(
	name = "Leaflet Details Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun LeafletDetailsScreenDarkPreview() {
	PreviewTheme(darkTheme = true) {
		LeafletDetailsScreen(state = previewLeafletDetailsState)
	}
}

@Preview(
	name = "My Submissions Populated Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Composable
fun MySubmissionsScreenPopulatedLightPreview() {
	PreviewTheme {
		MySubmissionsScreen(
			state = previewSubmissionsState,
			onEdit = {},
		)
	}
}

@Preview(
	name = "My Submissions Populated Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun MySubmissionsScreenPopulatedDarkPreview() {
	PreviewTheme(darkTheme = true) {
		MySubmissionsScreen(
			state = previewSubmissionsState,
			onEdit = {},
		)
	}
}

@Preview(
	name = "My Submissions Empty Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "My Submissions Empty Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun MySubmissionsScreenEmptyPreview() {
	PreviewTheme {
		MySubmissionsScreen(
			state = previewSubmissionsEmptyState,
			onEdit = {},
		)
	}
}

@Preview(
	name = "My Submissions Loading Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "My Submissions Loading Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun MySubmissionsScreenLoadingPreview() {
	PreviewTheme {
		MySubmissionsScreen(
			state = previewSubmissionsLoadingState,
			onEdit = {},
		)
	}
}

@Preview(
	name = "My Submissions Error Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "My Submissions Error Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun MySubmissionsScreenErrorPreview() {
	PreviewTheme {
		MySubmissionsScreen(
			state = previewSubmissionsErrorState,
			onEdit = {},
		)
	}
}

@Preview(
	name = "Pending Approvals Urgent + Normal Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Pending Approvals Urgent + Normal Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PendingApprovalsScreenQueuePreview() {
	PreviewTheme {
		PendingApprovalsScreen(
			state = previewPendingApprovalsState,
			onReview = {},
		)
	}
}

@Preview(
	name = "Pending Approvals Empty Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Pending Approvals Empty Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PendingApprovalsScreenEmptyPreview() {
	PreviewTheme {
		PendingApprovalsScreen(
			state = previewPendingApprovalsEmptyState,
			onReview = {},
		)
	}
}

@Preview(
	name = "Pending Approvals Loading Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Pending Approvals Loading Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PendingApprovalsScreenLoadingPreview() {
	PreviewTheme {
		PendingApprovalsScreen(
			state = previewPendingApprovalsLoadingState,
			onReview = {},
		)
	}
}

@Preview(
	name = "Pending Approvals Error Light",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
)
@Preview(
	name = "Pending Approvals Error Dark",
	group = "Design Theme",
	showBackground = true,
	widthDp = PreviewWidthDp,
	heightDp = PreviewHeightDp,
	uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PendingApprovalsScreenErrorPreview() {
	PreviewTheme {
		PendingApprovalsScreen(
			state = previewPendingApprovalsErrorState,
			onReview = {},
		)
	}
}
