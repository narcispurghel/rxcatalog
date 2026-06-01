package com.github.narcispurghel.rxcatalog.navigation

import android.net.Uri

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val HOME = "home"
    const val SEARCH = "search?query={query}"
    const val MEDICINE = "medicine/{medicineId}"
    const val LEAFLET = "leaflet/{leafletId}"
    const val SUBMIT = "submit?submissionId={submissionId}&medicineId={medicineId}"
    const val MY_SUBMISSIONS = "my-submissions"
    const val PENDING_APPROVALS = "pending-approvals"
    const val REVIEW = "review/{submissionId}"
    const val PROFILE = "profile"

    const val MEDICINE_DEEP_LINK = "rxcatalog://medicine/{medicineId}"
    const val LEAFLET_DEEP_LINK = "rxcatalog://leaflet/{leafletId}"
    const val SUBMISSION_DEEP_LINK = "rxcatalog://submission/{submissionId}"
    const val WEB_MEDICINE_DEEP_LINK = "https://rxcatalog.local/medicines/{medicineId}"

    fun searchRoute(query: String? = null): String {
        if (query.isNullOrBlank()) {
            return "search"
        }

        return "search?query=${Uri.encode(query)}"
    }

    fun medicineRoute(medicineId: String): String = "medicine/$medicineId"

    fun leafletRoute(leafletId: String): String = "leaflet/$leafletId"

    fun submitRoute(
        submissionId: String? = null,
        medicineId: String? = null,
    ): String {
        val queryParameters =
            buildList {
                submissionId?.takeIf { it.isNotBlank() }?.let {
                    add(
                        "submissionId=${Uri.encode(it)}",
                    )
                }
                medicineId?.takeIf { it.isNotBlank() }?.let { add("medicineId=${Uri.encode(it)}") }
            }

        return if (queryParameters.isEmpty()) {
            "submit"
        } else {
            "submit?${queryParameters.joinToString("&")}"
        }
    }

    fun reviewRoute(submissionId: String): String = "review/$submissionId"

    fun submissionDeepLink(submissionId: String): String = "rxcatalog://submission/$submissionId"

    fun medicineDeepLink(medicineId: String): String = "rxcatalog://medicine/$medicineId"

    fun leafletDeepLink(leafletId: String): String = "rxcatalog://leaflet/$leafletId"
}
