package com.github.narcispurghel.rxcatalog.network

import com.github.narcispurghel.rxcatalog.network.dto.CreateLeafletSubmissionRequestDto
import com.github.narcispurghel.rxcatalog.network.dto.CreateLeafletSubmissionResponseDto
import com.github.narcispurghel.rxcatalog.network.dto.MedicineDetailsResponseDto
import com.github.narcispurghel.rxcatalog.network.dto.MedicineSearchResponseDto
import com.github.narcispurghel.rxcatalog.network.dto.OpenFdaDrugLabelSearchResponseDto
import retrofit2.http.*

interface CatalogApiService {
	@GET("v1/medicines")
	suspend fun searchMedicines(
		@Query("query") query: String? = null,
	): MedicineSearchResponseDto

	@GET("v1/medicines/{medicineId}")
	suspend fun getMedicineDetails(
		@Path("medicineId") medicineId: String,
	): MedicineDetailsResponseDto

	@GET("v1/leaflets/{leafletId}")
	suspend fun getLeafletDetails(
		@Path("leafletId") leafletId: String,
	): MedicineDetailsResponseDto

	@POST("v1/medicines/{medicineId}/submissions")
	suspend fun submitLeaflet(
		@Path("medicineId") medicineId: String,
		@Body body: CreateLeafletSubmissionRequestDto,
	): CreateLeafletSubmissionResponseDto
}

interface OpenFdaApiService {
	@GET("drug/label.json")
	suspend fun searchDrugLabels(
		@Query("search") search: String,
		@Query("limit") limit: Int = 25,
	): OpenFdaDrugLabelSearchResponseDto
}
