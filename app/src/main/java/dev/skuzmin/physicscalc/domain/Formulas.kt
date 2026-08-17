package dev.skuzmin.physicscalc.domain

import kotlin.math.sqrt

/*
 * The whole calculator. Every entry here becomes a screen for free.
 *
 * ponytail: 23 explicit one-line solvers instead of a symbolic/product-form solver engine.
 * Six of the seven formulas are pure products and could auto-derive their inverses, but the
 * engine plus its two formula kinds costs about as many lines as just writing the algebra out,
 * reads worse, and would have to be torn open the first time a non-product formula arrives
 * (Eₖ = ½mv², projectile range). Add the engine only if this table reaches a few hundred rows.
 */

private val distance = Quantity("s", "Distance", "m")
private val speed = Quantity("v", "Speed", "m/s")
private val time = Quantity("t", "Time", "s")

private val finalVelocity = Quantity("v", "Final velocity", "m/s")
private val initialVelocity = Quantity("v₀", "Initial velocity", "m/s")
private val acceleration = Quantity("a", "Acceleration", "m/s²")

private val netForce = Quantity("F", "Net force", "N")
private val mass = Quantity("m", "Mass", "kg")

private val weight = Quantity("F", "Weight", "N")
private val gravity = Quantity("g", "Gravitational acceleration", "m/s²")

private val frictionForce = Quantity("F", "Friction force", "N")
private val frictionCoefficient = Quantity("μ", "Coefficient of friction", "")
private val normalForce = Quantity("N", "Normal force", "N")

private val buoyantForce = Quantity("F", "Buoyant force", "N")
private val density = Quantity("ρ", "Fluid density", "kg/m³")
private val volume = Quantity("V", "Displaced volume", "m³")

private val springForce = Quantity("F", "Spring force", "N")
private val springConstant = Quantity("k", "Spring constant", "N/m")
private val extension = Quantity("x", "Extension", "m")

private val kineticEnergy = Quantity("Eₖ", "Kinetic energy", "J")
private val potentialEnergy = Quantity("Eₚ", "Potential energy", "J")
private val height = Quantity("h", "Height", "m")
private val momentum = Quantity("p", "Momentum", "kg·m/s")

private val appliedForce = Quantity("F", "Force", "N")
private val work = Quantity("W", "Work", "J")
private val power = Quantity("P", "Power", "W")

private val pressure = Quantity("p", "Pressure", "Pa")
private val area = Quantity("A", "Area", "m²")

private val voltage = Quantity("U", "Voltage", "V")
private val current = Quantity("I", "Current", "A")
private val resistance = Quantity("R", "Resistance", "Ω")

object Formulas {

    val all: List<Formula> = listOf(
        Formula(
            id = "uniform_motion",
            title = "Uniform motion",
            expression = "s = v · t",
            explanation = "Distance covered by an object moving at constant speed. With no " +
                "acceleration, speed is just distance divided by the time taken.",
            solvers = listOf(
                Solver(distance, listOf(speed, time)) { (v, t) -> v * t },
                Solver(speed, listOf(distance, time)) { (s, t) -> s / t },
                Solver(time, listOf(distance, speed)) { (s, v) -> s / v },
            ),
        ),

        Formula(
            id = "uniform_acceleration",
            title = "Uniformly accelerated motion",
            expression = "v = v₀ + a · t",
            explanation = "Velocity of an object under constant acceleration. The object starts " +
                "at v₀ and gains a·t of velocity over time t. Use a negative acceleration for " +
                "deceleration.",
            solvers = listOf(
                Solver(finalVelocity, listOf(initialVelocity, acceleration, time)) { (v0, a, t) -> v0 + a * t },
                Solver(initialVelocity, listOf(finalVelocity, acceleration, time)) { (v, a, t) -> v - a * t },
                // The 2020 code wrote this as `v - v0 / t`, missing the parentheses.
                Solver(acceleration, listOf(finalVelocity, initialVelocity, time)) { (v, v0, t) -> (v - v0) / t },
                Solver(time, listOf(finalVelocity, initialVelocity, acceleration)) { (v, v0, a) -> (v - v0) / a },
            ),
        ),

        Formula(
            id = "newtons_second_law",
            title = "Newton's second law",
            expression = "F = m · a",
            explanation = "The net force on a body equals its mass times its acceleration. This " +
                "is the link between the forces acting on something and how its motion changes.",
            solvers = listOf(
                Solver(netForce, listOf(mass, acceleration)) { (m, a) -> m * a },
                Solver(mass, listOf(netForce, acceleration)) { (f, a) -> f / a },
                Solver(acceleration, listOf(netForce, mass)) { (f, m) -> f / m },
            ),
        ),

        Formula(
            id = "weight",
            title = "Weight",
            expression = "F = m · g",
            explanation = "The downward force gravity exerts on a mass. Near Earth's surface " +
                "g ≈ 9.81 m/s²; weight changes with location while mass does not.",
            solvers = listOf(
                Solver(weight, listOf(mass, gravity)) { (m, g) -> m * g },
                Solver(mass, listOf(weight, gravity)) { (f, g) -> f / g },
                Solver(gravity, listOf(weight, mass)) { (f, m) -> f / m },
            ),
        ),

        Formula(
            id = "sliding_friction",
            title = "Sliding friction",
            expression = "F = μ · N",
            explanation = "Friction opposing a sliding surface, proportional to how hard the " +
                "surfaces are pressed together. μ is dimensionless and depends on the pair of " +
                "materials, typically between 0.05 and 1.5.",
            solvers = listOf(
                Solver(frictionForce, listOf(frictionCoefficient, normalForce)) { (mu, n) -> mu * n },
                Solver(frictionCoefficient, listOf(frictionForce, normalForce)) { (f, n) -> f / n },
                Solver(normalForce, listOf(frictionForce, frictionCoefficient)) { (f, mu) -> f / mu },
            ),
        ),

        Formula(
            id = "archimedes",
            title = "Archimedes' principle",
            expression = "F = ρ · g · V",
            explanation = "The upward buoyant force on a submerged body equals the weight of the " +
                "fluid it displaces. Float or sink is decided by comparing this against the " +
                "body's own weight.",
            solvers = listOf(
                Solver(buoyantForce, listOf(density, gravity, volume)) { (rho, g, v) -> rho * g * v },
                Solver(density, listOf(buoyantForce, gravity, volume)) { (f, g, v) -> f / (g * v) },
                Solver(gravity, listOf(buoyantForce, density, volume)) { (f, rho, v) -> f / (rho * v) },
                Solver(volume, listOf(buoyantForce, density, gravity)) { (f, rho, g) -> f / (rho * g) },
            ),
        ),

        Formula(
            id = "hookes_law",
            title = "Hooke's law",
            expression = "F = k · x",
            explanation = "The restoring force of a spring is proportional to how far it is " +
                "stretched or compressed. Holds until the material's elastic limit is passed.",
            solvers = listOf(
                Solver(springForce, listOf(springConstant, extension)) { (k, x) -> k * x },
                Solver(springConstant, listOf(springForce, extension)) { (f, x) -> f / x },
                Solver(extension, listOf(springForce, springConstant)) { (f, k) -> f / k },
            ),
        ),

        Formula(
            id = "kinetic_energy",
            title = "Kinetic energy",
            expression = "Eₖ = ½ · m · v²",
            explanation = "The energy a moving body carries. It grows with the square of speed, " +
                "so doubling the speed quadruples the energy. Solving for speed returns the " +
                "positive root.",
            solvers = listOf(
                Solver(kineticEnergy, listOf(mass, speed)) { (m, v) -> 0.5 * m * v * v },
                Solver(mass, listOf(kineticEnergy, speed)) { (e, v) -> 2 * e / (v * v) },
                Solver(speed, listOf(kineticEnergy, mass)) { (e, m) -> sqrt(2 * e / m) },
            ),
        ),

        Formula(
            id = "potential_energy",
            title = "Gravitational potential energy",
            expression = "Eₚ = m · g · h",
            explanation = "The energy stored by lifting a mass against gravity. Only the change " +
                "in height matters, so h is measured from whatever reference level you choose.",
            solvers = listOf(
                Solver(potentialEnergy, listOf(mass, gravity, height)) { (m, g, h) -> m * g * h },
                Solver(mass, listOf(potentialEnergy, gravity, height)) { (e, g, h) -> e / (g * h) },
                Solver(gravity, listOf(potentialEnergy, mass, height)) { (e, m, h) -> e / (m * h) },
                Solver(height, listOf(potentialEnergy, mass, gravity)) { (e, m, g) -> e / (m * g) },
            ),
        ),

        Formula(
            id = "momentum",
            title = "Momentum",
            expression = "p = m · v",
            explanation = "Mass in motion. Total momentum is conserved in a closed system, which " +
                "is what makes collisions solvable.",
            solvers = listOf(
                Solver(momentum, listOf(mass, speed)) { (m, v) -> m * v },
                Solver(mass, listOf(momentum, speed)) { (p, v) -> p / v },
                Solver(speed, listOf(momentum, mass)) { (p, m) -> p / m },
            ),
        ),

        Formula(
            id = "work",
            title = "Work",
            expression = "W = F · s",
            explanation = "Energy transferred by a force pushing something through a distance. " +
                "This form assumes the force points along the displacement.",
            solvers = listOf(
                Solver(work, listOf(appliedForce, distance)) { (f, s) -> f * s },
                Solver(appliedForce, listOf(work, distance)) { (w, s) -> w / s },
                Solver(distance, listOf(work, appliedForce)) { (w, f) -> w / f },
            ),
        ),

        Formula(
            id = "power",
            title = "Power",
            expression = "P = W / t",
            explanation = "How fast work is done. The same job done in half the time needs twice " +
                "the power.",
            solvers = listOf(
                Solver(power, listOf(work, time)) { (w, t) -> w / t },
                Solver(work, listOf(power, time)) { (p, t) -> p * t },
                Solver(time, listOf(power, work)) { (p, w) -> w / p },
            ),
        ),

        Formula(
            id = "pressure",
            title = "Pressure",
            expression = "p = F / A",
            explanation = "Force spread over an area. The same force on a smaller area means " +
                "higher pressure, which is why a sharp blade cuts.",
            solvers = listOf(
                Solver(pressure, listOf(appliedForce, area)) { (f, a) -> f / a },
                Solver(appliedForce, listOf(pressure, area)) { (p, a) -> p * a },
                Solver(area, listOf(pressure, appliedForce)) { (p, f) -> f / p },
            ),
        ),

        Formula(
            id = "ohms_law",
            title = "Ohm's law",
            expression = "U = I · R",
            explanation = "The voltage across a resistor equals the current through it times its " +
                "resistance. The foundation of basic circuit analysis.",
            solvers = listOf(
                Solver(voltage, listOf(current, resistance)) { (i, r) -> i * r },
                Solver(current, listOf(voltage, resistance)) { (u, r) -> u / r },
                Solver(resistance, listOf(voltage, current)) { (u, i) -> u / i },
            ),
        ),
    )

    fun byId(id: String): Formula? = all.firstOrNull { it.id == id }

    /** Matches on title, expression, and every quantity name/symbol the formula mentions. */
    fun search(query: String): List<Formula> {
        val q = query.trim()
        if (q.isEmpty()) return all
        return all.filter { formula ->
            formula.title.contains(q, ignoreCase = true) ||
                formula.expression.contains(q, ignoreCase = true) ||
                formula.quantities.any {
                    it.name.contains(q, ignoreCase = true) || it.symbol.equals(q, ignoreCase = true)
                }
        }
    }
}
