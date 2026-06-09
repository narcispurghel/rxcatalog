package com.github.narcispurghel.rxcatalog.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicineDto(
    @SerialName("id")
    val id: String,
    @SerialName("canonical_name")
    val canonicalName: String,
    @SerialName("brand_name")
    val brandName: String? = null,
    @SerialName("active_ingredient")
    val activeIngredient: String? = null,
    @SerialName("atc_code")
    val atcCode: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("last_updated_at")
    val lastUpdatedAt: Long? = null,
)

@Serializable
data class MedicineSearchResponseDto(
    @SerialName("items")
    val items: List<MedicineDto> = emptyList(),
)

@Serializable
data class ApprovedLeafletDto(
    @SerialName("id")
    val id: String,
    @SerialName("medicine_id")
    val medicineId: String,
    @SerialName("title")
    val title: String,
    @SerialName("content")
    val content: String,
    @SerialName("version")
    val version: Int = 1,
    @SerialName("published_at")
    val publishedAt: Long? = null,
)

@Serializable
data class LeafletSubmissionDto(
    @SerialName("id")
    val id: String,
    @SerialName("medicine_id")
    val medicineId: String,
    @SerialName("submitted_by_user_id")
    val submittedByUserId: String,
    @SerialName("title")
    val title: String,
    @SerialName("content")
    val content: String,
    @SerialName("status")
    val status: String,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_at")
    val updatedAt: Long,
)

@Serializable
data class MedicineDetailsResponseDto(
    @SerialName("medicine")
    val medicine: MedicineDto,
    @SerialName("approved_leaflet")
    val approvedLeaflet: ApprovedLeafletDto? = null,
    @SerialName("recent_submissions")
    val recentSubmissions: List<LeafletSubmissionDto> = emptyList(),
)

@Serializable
data class CreateLeafletSubmissionRequestDto(
    @SerialName("medicine_id")
    val medicineId: String,
    @SerialName("submitted_by_user_id")
    val submittedByUserId: String,
    @SerialName("title")
    val title: String,
    @SerialName("content")
    val content: String,
)

@Serializable
data class CreateLeafletSubmissionResponseDto(
    @SerialName("submission")
    val submission: LeafletSubmissionDto,
)
