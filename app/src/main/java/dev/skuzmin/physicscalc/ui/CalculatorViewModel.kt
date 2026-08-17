package dev.skuzmin.physicscalc.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.skuzmin.physicscalc.data.HistoryEntry
import dev.skuzmin.physicscalc.data.HistoryRepository
import dev.skuzmin.physicscalc.domain.Formula
import dev.skuzmin.physicscalc.domain.Formulas
import dev.skuzmin.physicscalc.domain.Quantity
import dev.skuzmin.physicscalc.domain.parseNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val formula: Formula? = null,
    val targetSymbol: String = "",
    /** Raw text per input symbol, exactly as typed. */
    val inputs: Map<String, String> = emptyMap(),
    val result: Double? = null,
    /** Symbols the user has typed something unparseable into. */
    val invalidSymbols: Set<String> = emptySet(),
    val saved: Boolean = false,
) {
    val solver get() = formula?.solverFor(targetSymbol)
    val target: Quantity? get() = solver?.target

    /** Only a finite result is worth keeping; a division by zero is not a calculation. */
    val canSave: Boolean get() = result?.isFinite() == true && !saved
}

/**
 * The one ViewModel behind all 23 calculators. Results recompute on every keystroke, so there
 * is no calculate button and no error toast — a field is either blank, flagged, or contributing.
 */
class CalculatorViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val history = HistoryRepository(application)

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    init {
        val route = savedStateHandle.toRoute<CalculatorRoute>()
        // Null when a stored history entry outlives the formula it referenced.
        val formula = Formulas.byId(route.formulaId)
        _state.value = CalculatorUiState(
            formula = formula,
            targetSymbol = formula?.solvers?.first()?.target?.symbol.orEmpty(),
        )
        route.entryId?.let { restore(it) }
    }

    private fun restore(entryId: Long) = viewModelScope.launch {
        val entry = history.entries.first().firstOrNull { it.savedAt == entryId } ?: return@launch
        _state.update {
            it.copy(targetSymbol = entry.targetSymbol, inputs = entry.inputs, saved = true).recomputed()
        }
    }

    /** Switching which quantity to solve for clears the inputs, since the field set changes. */
    fun selectTarget(symbol: String) = _state.update {
        it.copy(
            targetSymbol = symbol,
            inputs = emptyMap(),
            result = null,
            invalidSymbols = emptySet(),
            saved = false,
        )
    }

    fun updateInput(symbol: String, text: String) = _state.update { current ->
        current.copy(inputs = current.inputs + (symbol to text), saved = false).recomputed()
    }

    fun save() {
        val current = _state.value
        val formula = current.formula ?: return
        val result = current.result?.takeIf { it.isFinite() } ?: return

        _state.update { it.copy(saved = true) }
        viewModelScope.launch {
            history.add(
                HistoryEntry(
                    formulaId = formula.id,
                    targetSymbol = current.targetSymbol,
                    inputs = current.inputs,
                    result = result,
                    savedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun CalculatorUiState.recomputed(): CalculatorUiState {
        val solver = solver ?: return this
        val parsed = solver.inputs.map { it.symbol to parseNumber(inputs[it.symbol].orEmpty()) }

        // Only flag fields the user has actually typed into; blank ones are merely incomplete.
        val invalid = parsed
            .filter { (symbol, value) -> value == null && !inputs[symbol].isNullOrBlank() }
            .map { it.first }
            .toSet()

        val values = parsed.map { it.second }
        val result = if (values.any { it == null }) null else solver.compute(values.filterNotNull())

        return copy(result = result, invalidSymbols = invalid)
    }
}
