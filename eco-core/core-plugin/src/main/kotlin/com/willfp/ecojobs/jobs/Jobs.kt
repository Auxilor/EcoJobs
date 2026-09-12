package com.willfp.ecojobs.jobs

import com.google.common.collect.ImmutableList
import com.willfp.eco.core.Eco
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.leaderboard.Leaderboards
import com.willfp.eco.core.leaderboard.PlayerbaseTally
import com.willfp.eco.core.registry.Registry
import com.willfp.ecojobs.api.activeJobsKey
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.api.legacyActiveJobKey
import com.willfp.ecojobs.plugin
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import org.bukkit.OfflinePlayer

object Jobs : ConfigCategory("job", "jobs") {
    private val registry = Registry<Job>()

    /**
     * Get all registered [Job]s.
     *
     * @return A list of all [Job]s.
     */
    @JvmStatic
    fun values(): List<Job> {
        return ImmutableList.copyOf(registry.values())
    }

    /**
     * Get [Job] matching ID.
     *
     * @param name The name to search for.
     * @return The matching [Job], or null if not found.
     */
    @JvmStatic
    fun getByID(name: String?): Job? {
        return name?.let { registry[it] }
    }

    /**
     * The tally counting how many players have each job active, keyed by job ID.
     */
    var tally: PlayerbaseTally? = null
        private set

    /**
     * Register the playerbase tally backing %<job>_total_players%.
     *
     * One tally for every job: the active jobs of every player live in a single list key, so a
     * single bulk read produces the count for every job at once.
     *
     * Reads [activeJobsKey] and [legacyActiveJobKey] directly rather than going through
     * OfflinePlayer.activeJobs, which migrates the legacy key as a side effect of being read and
     * would therefore write to two profiles per unmigrated player on every refresh.
     */
    internal fun registerTally() {
        tally = Leaderboards.registerTally(plugin, "active_jobs") { uuids ->
            val counts = HashMap<String, Int>()

            val active = Eco.get().readAllProfileValues(uuids, activeJobsKey)
            val legacy = Eco.get().readAllProfileValues(uuids, legacyActiveJobKey)

            for (uuid in uuids) {
                val ids = active[uuid]?.takeIf { it.isNotEmpty() }
                    ?: legacy[uuid]?.takeIf { it.isNotBlank() }?.let { listOf(it) }
                    ?: continue

                for (id in ids.distinct()) {
                    counts.merge(id, 1, Int::plus)
                }
            }

            counts
        }
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Job(id, config))
    }

    /**
     * A players unlocked jobs.
     */
    val OfflinePlayer.unlockedJobs: List<Job>
        get() = values()
            .sortedByDescending { this.getJobLevel(it) }
            .filter { this.getJobLevel(it) > 0 }
}
