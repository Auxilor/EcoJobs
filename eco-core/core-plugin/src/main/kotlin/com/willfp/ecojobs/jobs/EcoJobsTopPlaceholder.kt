package com.willfp.ecojobs.jobs

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.placeholder.RegistrablePlaceholder
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.util.savedDisplayName
import java.util.regex.Pattern

class EcoJobsJobTopPlaceholder(
    private val plugin: EcoPlugin
) : RegistrablePlaceholder {
    private val pattern = Pattern.compile("(top_)[a-z]+_[0-9]+_[a-z]+")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyPosition: String = plugin.langYml.getString("top.empty-position")
        val args = params.split("_")

        if (args.size < 3) {
            return null
        }

        if (args[0] != "top") {
            return null
        }

        val job = Jobs.getByID(args[1]) ?: return null

        val place = args[2].toIntOrNull() ?: return null

        return when (args.last()) {
            "name" -> job.getTop(place)?.player?.savedDisplayName ?: emptyPosition
            "level", "amount" -> job.getTop(place)?.level?.toString() ?: emptyPosition
            else -> null
        }
    }
}