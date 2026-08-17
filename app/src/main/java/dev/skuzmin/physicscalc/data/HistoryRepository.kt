package dev.skuzmin.physicscalc.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class HistoryEntry(
    val formulaId: String,
    val targetSymbol: String,
    /** Raw text per input symbol, so reopening restores exactly what was typed. */
    val inputs: Map<String, String>,
    val result: Double,
    /** Doubles as the entry's identity — good enough at one save per user action. */
    val savedAt: Long,
)

private val Context.historyStore by preferencesDataStore(name = "history")

private const val MAX_ENTRIES = 100

/**
 * ponytail: the whole history is one JSON string in DataStore. Fine for a capped list this size,
 * and it costs one dependency the app already needs for type-safe navigation. Swap in Room the
 * moment history needs querying, filtering, or grows past a few hundred entries.
 */
class HistoryRepository(private val context: Context) {

    private val key = stringPreferencesKey("entries")
    private val json = Json { ignoreUnknownKeys = true }

    /** Newest first. Unreadable stored data is treated as empty rather than crashing the app. */
    val entries: Flow<List<HistoryEntry>> = context.historyStore.data.map { prefs ->
        prefs[key]?.let { stored ->
            runCatching { json.decodeFromString<List<HistoryEntry>>(stored) }.getOrNull()
        }.orEmpty()
    }

    suspend fun add(entry: HistoryEntry) {
        context.historyStore.edit { prefs ->
            val current = prefs[key]
                ?.let { runCatching { json.decodeFromString<List<HistoryEntry>>(it) }.getOrNull() }
                .orEmpty()
            prefs[key] = json.encodeToString((listOf(entry) + current).take(MAX_ENTRIES))
        }
    }

    suspend fun clear() {
        context.historyStore.edit { it.remove(key) }
    }
}
