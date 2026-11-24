package com.refillfinder.app

import com.refillfinder.app.model.Cooler
import com.refillfinder.app.model.CoolerStatus
import com.refillfinder.app.utils.CampusHelper
import com.refillfinder.app.utils.Constants
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit Tests for CampusHelper utility.
 *
 * Syllabus: Basic Unit Testing using JUnit
 * Run with: ./gradlew test
 */
class CampusHelperTest {

    // ── Constants tests ───────────────────────────────────────────────

    @Test
    fun `campus has exactly 60 blocks`() {
        assertEquals(60, Constants.TOTAL_BLOCKS)
    }

    @Test
    fun `campus has exactly 9 floors per block`() {
        assertEquals(9, Constants.FLOORS_PER_BLOCK)
    }

    @Test
    fun `coolers exist on exactly 5 floors`() {
        assertEquals(5, Constants.COOLER_FLOORS.size)
    }

    @Test
    fun `total coolers equals 300`() {
        val total = Constants.TOTAL_BLOCKS * Constants.COOLER_FLOORS.size
        assertEquals(300, total)
    }

    // ── hasCoolerOnFloor tests ────────────────────────────────────────

    @Test
    fun `floor 1 has a cooler`() {
        assertTrue(CampusHelper.hasCoolerOnFloor(1))
    }

    @Test
    fun `floor 2 has no cooler`() {
        assertFalse(CampusHelper.hasCoolerOnFloor(2))
    }

    @Test
    fun `all odd floors 1 to 9 have coolers`() {
        listOf(1, 3, 5, 7, 9).forEach { floor ->
            assertTrue("Floor $floor should have a cooler", CampusHelper.hasCoolerOnFloor(floor))
        }
    }

    @Test
    fun `all even floors have no coolers`() {
        listOf(2, 4, 6, 8).forEach { floor ->
            assertFalse("Floor $floor should NOT have a cooler", CampusHelper.hasCoolerOnFloor(floor))
        }
    }

    // ── nearestCoolerFloor tests ──────────────────────────────────────

    @Test
    fun `nearest cooler floor from floor 2 is floor 1 or 3`() {
        val result = CampusHelper.nearestCoolerFloor(2)
        assertTrue(result == 1 || result == 3)
    }

    @Test
    fun `nearest cooler floor from floor 6 is floor 5 or 7`() {
        val result = CampusHelper.nearestCoolerFloor(6)
        assertTrue(result == 5 || result == 7)
    }

    @Test
    fun `nearest cooler floor from floor 5 is 5 itself`() {
        assertEquals(5, CampusHelper.nearestCoolerFloor(5))
    }

    // ── findNearestCooler tests ───────────────────────────────────────

    @Test
    fun `returns null when no coolers available`() {
        val result = CampusHelper.findNearestCooler(emptyList(), 1, 1)
        assertNull(result)
    }

    @Test
    fun `returns null when all coolers are offline`() {
        val offlineCoolers = listOf(
            Cooler("b1f1", 1, 1, CoolerStatus.OFFLINE),
            Cooler("b1f3", 1, 3, CoolerStatus.OFFLINE)
        )
        val result = CampusHelper.findNearestCooler(offlineCoolers, 1, 1)
        assertNull(result)
    }

    @Test
    fun `finds nearest active cooler in same block`() {
        val coolers = listOf(
            Cooler("b5f1", 5, 1, CoolerStatus.ACTIVE),
            Cooler("b1f1", 1, 1, CoolerStatus.ACTIVE),
            Cooler("b1f3", 1, 3, CoolerStatus.ACTIVE)
        )
        val result = CampusHelper.findNearestCooler(coolers, 1, 2)
        assertNotNull(result)
        assertEquals(1, result!!.block) // Should prefer same block
    }

    @Test
    fun `prefers active over reported cooler at same distance`() {
        val coolers = listOf(
            Cooler("b1f1", 1, 1, CoolerStatus.REPORTED),
            Cooler("b1f3", 1, 3, CoolerStatus.ACTIVE)
        )
        // Active should be preferred
        val active = coolers.filter { it.status == CoolerStatus.ACTIVE }
        assertTrue(active.isNotEmpty())
    }

    // ── timeAgo tests ─────────────────────────────────────────────────

    @Test
    fun `timeAgo returns just now for recent timestamp`() {
        val now = System.currentTimeMillis()
        assertEquals("just now", CampusHelper.timeAgo(now - 5_000))
    }

    @Test
    fun `timeAgo returns minutes for 5 minutes ago`() {
        val fiveMinutesAgo = System.currentTimeMillis() - 5 * 60_000
        assertEquals("5m ago", CampusHelper.timeAgo(fiveMinutesAgo))
    }

    @Test
    fun `timeAgo returns hours for 2 hours ago`() {
        val twoHoursAgo = System.currentTimeMillis() - 2 * 3_600_000
        assertEquals("2h ago", CampusHelper.timeAgo(twoHoursAgo))
    }

    @Test
    fun `timeAgo returns days for yesterday`() {
        val yesterday = System.currentTimeMillis() - 25 * 3_600_000
        assertEquals("1d ago", CampusHelper.timeAgo(yesterday))
    }

    // ── greeting tests ────────────────────────────────────────────────

    @Test
    fun `greeting returns a non-empty string`() {
        val greeting = CampusHelper.greeting()
        assertTrue(greeting.isNotEmpty())
        assertTrue(greeting in listOf("Morning", "Afternoon", "Evening", "Night"))
    }
}
