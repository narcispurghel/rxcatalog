package com.github.narcispurghel.rxcatalog.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRoutesTest {
    @Test
    fun `core route builders produce the expected paths`() {
        assertEquals("medicine/med-001", AppRoutes.medicineRoute("med-001"))
        assertEquals("leaflet/leaf-001", AppRoutes.leafletRoute("leaf-001"))
        assertEquals("review/review-001", AppRoutes.reviewRoute("review-001"))
        assertEquals(
            "rxcatalog://review/review-001",
            AppRoutes.reviewDeepLink("review-001"),
        )
        assertEquals(
            "rxcatalog://medicine/med-001",
            AppRoutes.medicineDeepLink("med-001"),
        )
        assertEquals(
            "rxcatalog://leaflet/leaf-001",
            AppRoutes.leafletDeepLink("leaf-001"),
        )
        assertEquals(
            "rxcatalog://submission/submission-001",
            AppRoutes.submissionDeepLink("submission-001"),
        )
    }

    @Test
    fun `blank search routes fall back to the base search destination`() {
        assertEquals("search", AppRoutes.searchRoute())
        assertEquals("search", AppRoutes.searchRoute(""))
        assertEquals("search", AppRoutes.searchRoute("   "))
    }
}
