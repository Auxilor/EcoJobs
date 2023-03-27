package com.willfp.ecojobs.jobs

import com.google.common.collect.ImmutableList
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.eco.core.registry.Registry
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.getJobLevel
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
    fun getByID(name: String): Job? {
        return registry[name]
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Job(id, config, plugin as EcoJobsPlugin))
    }

    /**
     * A players unlocked jobs.
     */
    val OfflinePlayer.unlockedJobs: List<Job>
        get() = values()
            .sortedByDescending { this.getJobLevel(it) }
            .filter { this.getJobLevel(it) > 0 }
}
