package dev.skuzmin.physicscalc.domain

import java.math.BigDecimal
import java.math.MathContext
import java.util.Locale
import kotlin.math.abs

/** A physical quantity: what to label a field and what unit it carries. */
data class Quantity(
    val symbol: String,
    val name: String,
    /** SI unit, or blank for dimensionless quantities such as the friction coefficient. */
    val unit: String,
)

/**
 * One rearrangement of a formula: which quantity it produces, which it needs, and the arithmetic.
 * [inputs] is positional — [compute] receives the values in the same order.
 */
data class Solver(
    val target: Quantity,
    val inputs: List<Quantity>,
    val compute: (List<Double>) -> Double,
)

data class Formula(
    val id: String,
    val title: String,
    /** Canonical form, shown to the reader, e.g. "F = ρ · g · V". */
    val expression: String,
    val explanation: String,
    val solvers: List<Solver>,
) {
    fun solverFor(symbol: String): Solver? = solvers.firstOrNull { it.target.symbol == symbol }

    /** Every quantity this formula mentions, for search. */
    val quantities: List<Quantity>
        get() = solvers.flatMap { listOf(it.target) + it.inputs }.distinctBy { it.symbol }
}

/**
 * Accepts what a person actually types: surrounding spaces, a comma decimal separator,
 * ".5", "5.", and exponent notation. Rejects anything else, including NaN and infinity.
 *
 * The 2020 version used the regex "-?\d+(\.\d+)?", which rejected all of those.
 */
fun parseNumber(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

/**
 * Formats to 6 significant digits without binary float noise — the old code called
 * String.valueOf(double) directly and rendered results like "0.30000000000000004".
 */
fun formatNumber(value: Double): String = when {
    !value.isFinite() -> "—"
    value == 0.0 -> "0"
    abs(value) >= 1e7 || abs(value) < 1e-4 -> String.format(Locale.US, "%.4e", value)
    else -> BigDecimal(value).round(MathContext(6)).stripTrailingZeros().toPlainString()
}
