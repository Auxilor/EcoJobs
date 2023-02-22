package com.willfp.ecojobs.jobs

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableList
import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.eco.core.price.Prices
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.PriceFactoryJobLevel
import org.bukkit.OfflinePlayer

object Jobs {
    private val BY_ID: BiMap<String, Job> = HashBiMap.create()

    /**
     * Get all registered [Job]s.
     *
     * @return A list of all [Job]s.
     */
    @JvmStatic
    fun values(): List<Job> {
        return ImmutableList.copyOf(BY_ID.values)
    }

    /**
     * Get [Job] matching ID.
     *
     * @param name The name to search for.
     * @return The matching [Job], or null if not found.
     */
    @JvmStatic
    fun getByID(name: String): Job? {
        return BY_ID[name]
    }

    /**
     * Update all [Job]s.
     *
     * @param plugin Instance of EcoJobs.
     */
    @ConfigUpdater
    @JvmStatic
    fun update(plugin: EcoJobsPlugin) {
        for (job in values()) {
            removeJob(job)
        }

        for ((id, jobConfig) in plugin.fetchConfigs("jobs")) {
            addNewJob(Job(id, jobConfig, plugin))
        }

        values().forEach {
            Prices.registerPriceFactory(PriceFactoryJobLevel(it))
        }
    }

    /**
     * Add new [Job] to EcoJobs.
     *
     * @param job The [Job] to add.
     */
    @JvmStatic
    fun addNewJob(job: Job) {
        BY_ID.remove(job.id)
        BY_ID[job.id] = job
    }

    /**
     * Remove [Job] from EcoJobs.
     *
     * @param job The [Job] to remove.
     */
    @JvmStatic
    fun removeJob(job: Job) {
        BY_ID.remove(job.id)
    }

    /**
     * A players unlocked jobs.
     */
    val OfflinePlayer.unlockedJobs: List<Job>
        get() = values()
            .sortedByDescending { this.getJobLevel(it) }
            .filter { this.getJobLevel(it) > 0 }
}
