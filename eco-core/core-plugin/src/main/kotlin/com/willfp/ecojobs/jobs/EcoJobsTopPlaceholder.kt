package com.willfp.ecojobs.jobs

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.placeholder.RegistrablePlaceholder
import com.willfp.eco.core.placeholder.context.PlaceholderContext
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecojobs.jobs.JobsLeaderboard.getTop
import java.util.regex.Pattern

class EcoJobsJobTopPlaceholder(
    private val plugin: EcoPlugin
) : RegistrablePlaceholder {
    private val pattern = Pattern.compile("top_([a-z0-9_]+)_(\\d+)_(name|level|amount)")

    override fun getPattern(): Pattern = pattern
    override fun getPlugin(): EcoPlugin = plugin

    override fun getValue(params: String, ctx: PlaceholderContext): String? {
        val emptyPosition: String = plugin.langYml.getString("top.empty-position")

        val matcher = pattern.matcher(params)
        if (!matcher.matches()) {
            return null
        }

        val jobId = matcher.group(1)
        val place = matcher.group(2).toIntOrNull() ?: return null
        val type = matcher.group(3)

        val job = Jobs.getByID(jobId) ?: return null

        return when (type) {
            "name" -> getTop(job, place)?.player?.savedDisplayName ?: emptyPosition
            "level", "amount" -> getTop(job, place)?.level?.toString() ?: emptyPosition
            else -> null
        }
    }
}