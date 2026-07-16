package com.adgpt.app.presentation.startup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupVideoResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun findStartupVideo(): String? {
        val candidates = buildList {
            addAll(context.assets.list("startup").orEmpty().map { "startup/$it" })
            addAll(context.assets.list("").orEmpty())
        }
        return candidates
            .filter { name ->
            name.endsWith(".mp4", ignoreCase = true) ||
                name.endsWith(".webm", ignoreCase = true) ||
                name.endsWith(".mkv", ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<String> { it.equals("Same_but_in_vertical_not_horiz.mp4", ignoreCase = true) }
                    .thenByDescending { it.contains("vertical", ignoreCase = true) }
                    .thenBy { it }
            )
            .firstOrNull()
    }
}
