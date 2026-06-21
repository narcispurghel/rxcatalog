package com.github.narcispurghel.rxcatalog.navigation

import com.github.narcispurghel.rxcatalog.common.UserRole
import org.junit.Assert.*
import org.junit.Test

class AppDestinationTest {
	@Test
	fun `authenticated destinations stay in the expected order`() {
		assertEquals(
			listOf(
				AppRoutes.HOME,
				AppRoutes.SEARCH,
				AppRoutes.MY_SUBMISSIONS,
				AppRoutes.PENDING_APPROVALS,
				AppRoutes.PROFILE,
			),
			authenticatedDestinations.map { it.route },
		)

		assertEquals(
			listOf(
				"Home",
				"Search",
				"Submissions",
				"Approvals",
				"Profile",
			),
			authenticatedDestinations.map { it.label },
		)
	}

	@Test
	fun `approvals destination is restricted to reviewer roles`() {
		val approvalsDestination =
			authenticatedDestinations.first { it.route == AppRoutes.PENDING_APPROVALS }

		assertFalse(approvalsDestination.isVisibleFor(null))
		assertFalse(approvalsDestination.isVisibleFor(UserRole.USER))
		assertTrue(approvalsDestination.isVisibleFor(UserRole.DOCTOR))
		assertTrue(approvalsDestination.isVisibleFor(UserRole.PHARMACIST))
		assertEquals(setOf(UserRole.DOCTOR, UserRole.PHARMACIST), approvalsDestination.allowedRoles)
	}

	@Test
	fun `non restricted destinations are visible for every role`() {
		val openDestinations =
			authenticatedDestinations.filter { it.allowedRoles.isEmpty() }

		assertTrue(openDestinations.isNotEmpty())
		openDestinations.forEach { destination ->
			assertTrue(destination.isVisibleFor(null))
			assertTrue(destination.isVisibleFor(UserRole.USER))
			assertTrue(destination.isVisibleFor(UserRole.DOCTOR))
			assertTrue(destination.isVisibleFor(UserRole.PHARMACIST))
		}
	}
}
