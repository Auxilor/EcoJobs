package com.willfp.ecojobs.placeholders

import com.willfp.eco.util.savedDisplayName
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.jobs.Jobs
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class EcoJobsTopExpansion(val plugin: EcoJobsPlugin): PlaceholderExpansion() {
    /**
     * The placeholder identifier of this expansion. May not contain %,
     * {} or _
     *
     * @return placeholder identifier that is associated with this expansion
     */
    override fun getIdentifier(): String {
        return "ecojobstop"
    }

    /**
     * The author of this expansion
     *
     * @return name of the author for this expansion
     */
    override fun getAuthor(): String {
        return "_OfTeN_"
    }

    /**
     * The version of this expansion
     *
     * @return current version of this expansion
     */
    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val args = params.split("_")
        val skill = Jobs.getByID(args.firstOrNull()?.lowercase() ?: return "Invalid job")
        val place = args.getOrNull(1)?.toIntOrNull() ?: return "Invalid place (must be an integer)"

        return when (args.lastOrNull() ?: return "Invalid type: ${args.lastOrNull()}. Must be name/amount") {
            "name" -> skill?.getTop(place)?.player?.savedDisplayName ?: plugin.langYml.getFormattedString(
                "top.name-empty"
            )
            "amount" -> skill?.getTop(place)?.amount?.toString() ?: plugin.langYml.getFormattedString(
                "top.amount-empty"
            )
            else -> null
        }
    }
}