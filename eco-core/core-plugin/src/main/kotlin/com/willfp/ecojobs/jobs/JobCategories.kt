package com.willfp.ecojobs.jobs

import com.google.common.collect.ImmutableList
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory

object JobCategories : ConfigCategory("category", "categories") {
    private val registry = Registry<JobCategory>()

    /**
     * Get all registered [JobCategory]s.
     *
     * @return A list of all [JobCategory]s.
     */
    @JvmStatic
    fun values(): List<JobCategory> = ImmutableList.copyOf(registry.values())

    /**
     * Get [JobCategory] matching ID.
     *
     * @param id The ID to search for.
     * @return The matching [JobCategory], or null if not found.
     */
    @JvmStatic
    fun getByID(id: String?): JobCategory? = id?.let { registry[it] }

    override fun clear(plugin: LibreforgePlugin) {
        for (category in registry.values()) {
            category.remove()
        }

        registry.clear()
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(JobCategory(id, config))
    }
}
