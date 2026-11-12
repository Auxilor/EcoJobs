package com.willfp.ecojobs.jobs

import com.google.common.collect.ImmutableList
import wiki.ecohub.config.Config
import wiki.ecohub.config.category.NativeConfigCategory
import wiki.ecohub.core.plugin.EcoPlugin
import wiki.ecohub.core.registry.Registry

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
