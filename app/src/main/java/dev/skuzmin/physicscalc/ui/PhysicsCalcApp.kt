package dev.skuzmin.physicscalc.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
object FormulaListRoute

@Serializable
object HistoryRoute

/** [entryId] reopens a saved calculation; null means a blank calculator. */
@Serializable
data class CalculatorRoute(val formulaId: String, val entryId: Long? = null)

@Composable
fun PhysicsCalcApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = FormulaListRoute) {
        composable<FormulaListRoute> {
            FormulaListScreen(
                onFormulaClick = { navController.navigate(CalculatorRoute(it.id)) },
                onHistoryClick = { navController.navigate(HistoryRoute) },
            )
        }
        composable<HistoryRoute> {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onEntryClick = { entry ->
                    navController.navigate(CalculatorRoute(entry.formulaId, entry.savedAt))
                },
            )
        }
        composable<CalculatorRoute> {
            CalculatorScreen(onBack = { navController.popBackStack() })
        }
    }
}
