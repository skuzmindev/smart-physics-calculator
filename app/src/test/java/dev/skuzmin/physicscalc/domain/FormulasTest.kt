package dev.skuzmin.physicscalc.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * One physically consistent set of values per formula. Every solver in that formula must
 * reproduce its own target from the others, so a single wrong rearrangement fails the build.
 */
private val consistentValues: Map<String, Map<String, Double>> = mapOf(
    "uniform_motion" to mapOf("s" to 12.0, "v" to 4.0, "t" to 3.0),
    "uniform_acceleration" to mapOf("v" to 20.0, "v₀" to 5.0, "a" to 3.0, "t" to 5.0),
    "newtons_second_law" to mapOf("F" to 12.0, "m" to 4.0, "a" to 3.0),
    "weight" to mapOf("F" to 19.62, "m" to 2.0, "g" to 9.81),
    "sliding_friction" to mapOf("F" to 3.0, "μ" to 0.3, "N" to 10.0),
    "archimedes" to mapOf("F" to 19.62, "ρ" to 1000.0, "g" to 9.81, "V" to 0.002),
    "hookes_law" to mapOf("F" to 10.0, "k" to 200.0, "x" to 0.05),
    "kinetic_energy" to mapOf("Eₖ" to 9.0, "m" to 2.0, "v" to 3.0),
    "potential_energy" to mapOf("Eₚ" to 98.1, "m" to 2.0, "g" to 9.81, "h" to 5.0),
    "momentum" to mapOf("p" to 6.0, "m" to 2.0, "v" to 3.0),
    "work" to mapOf("W" to 30.0, "F" to 10.0, "s" to 3.0),
    "power" to mapOf("P" to 6.0, "W" to 30.0, "t" to 5.0),
    "pressure" to mapOf("p" to 5.0, "F" to 20.0, "A" to 4.0),
    "ohms_law" to mapOf("U" to 10.0, "I" to 2.0, "R" to 5.0),
)

class FormulasTest {

    @Test
    fun `every solver reproduces its target from a consistent set of values`() {
        for (formula in Formulas.all) {
            val values = consistentValues[formula.id]
                ?: error("No test values defined for formula '${formula.id}'")

            for (solver in formula.solvers) {
                val args = solver.inputs.map { input ->
                    values[input.symbol]
                        ?: error("${formula.id}: no value for input '${input.symbol}'")
                }
                val expected = values[solver.target.symbol]
                    ?: error("${formula.id}: no value for target '${solver.target.symbol}'")

                assertEquals(
                    "${formula.id}: solving for ${solver.target.symbol} from " +
                        solver.inputs.joinToString { it.symbol },
                    expected,
                    solver.compute(args),
                    1e-9,
                )
            }
        }
    }

    @Test
    fun `each solver takes exactly the other quantities of its formula as inputs`() {
        for (formula in Formulas.all) {
            for (solver in formula.solvers) {
                val expected = formula.quantities.map { it.symbol }.toSet() - solver.target.symbol
                assertEquals(
                    "${formula.id}: inputs for ${solver.target.symbol}",
                    expected,
                    solver.inputs.map { it.symbol }.toSet(),
                )
            }
        }
    }

    @Test
    fun `catalog is well formed`() {
        assertEquals("formula count", 14, Formulas.all.size)
        assertEquals("solver count", 45, Formulas.all.sumOf { it.solvers.size })

        val ids = Formulas.all.map { it.id }
        assertEquals("formula ids are unique", ids.size, ids.toSet().size)

        for (formula in Formulas.all) {
            val targets = formula.solvers.map { it.target.symbol }
            assertEquals("${formula.id}: one solver per quantity", targets.size, targets.toSet().size)
            assertTrue("${formula.id}: has an explanation", formula.explanation.length > 30)
            assertNotNull("${formula.id}: findable by id", Formulas.byId(formula.id))
        }
    }

    @Test
    fun `search matches title, symbol and quantity name`() {
        assertEquals(Formulas.all.size, Formulas.search("   ").size)
        assertTrue(Formulas.search("hooke").any { it.id == "hookes_law" })
        assertTrue(Formulas.search("buoyant").any { it.id == "archimedes" })
        assertTrue(Formulas.search("μ").any { it.id == "sliding_friction" })
        assertTrue(Formulas.search("zzzz").isEmpty())
    }

    @Test
    fun `parseNumber accepts what people actually type`() {
        assertEquals(0.5, parseNumber(" .5 ")!!, 1e-12)
        assertEquals(5.0, parseNumber("5.")!!, 1e-12)
        assertEquals(1000.0, parseNumber("1e3")!!, 1e-12)
        assertEquals(-2.5, parseNumber("-2,5")!!, 1e-12)
        assertNull(parseNumber(""))
        assertNull(parseNumber("abc"))
        assertNull(parseNumber("Infinity"))
        assertNull(parseNumber("NaN"))
    }

    @Test
    fun `formatNumber hides binary float noise`() {
        assertEquals("0.3", formatNumber(0.1 + 0.2))
        assertEquals("12", formatNumber(12.0))
        assertEquals("0", formatNumber(0.0))
        assertEquals("—", formatNumber(Double.POSITIVE_INFINITY))
        assertEquals("9.81", formatNumber(9.81))
        assertTrue(formatNumber(1.23e12).contains("e"))
    }
}
