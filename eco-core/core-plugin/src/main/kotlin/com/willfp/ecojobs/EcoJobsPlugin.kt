package com.willfp.ecojobs

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.integrations.IntegrationLoader
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.toNiceString
import com.willfp.eco.util.toSingletonList
import com.willfp.ecojobs.commands.CommandEcojobs
import com.willfp.ecojobs.commands.CommandJobs
import com.willfp.ecojobs.jobs.*
import com.willfp.ecojobs.placeholders.EcoJobsTopExpansion
import com.willfp.libreforge.LibReforgePlugin
import org.bukkit.event.Listener

class EcoJobsPlugin : LibReforgePlugin() {
    init {
        instance = this
        registerHolderProvider { it.activeJobLevel?.toSingletonList() ?: emptyList() }
    }

    override fun handleEnableAdditional() {
        this.copyConfigs("jobs")

        PlayerPlaceholder(
            this,
            "job"
        ) { it.activeJob?.name ?: "" }.register()

        PlayerPlaceholder(
            this,
            "job_level"
        ) { it.activeJobLevel?.level?.toString() ?: "" }.register()

        PlayerPlaceholder(
            this,
            "job_id"
        ) { it.activeJob?.id ?: "" }.register()

        PlayerPlaceholder(
            this,
            "percentage_progress"
        ) {
            (it.activeJob?.let { it1 -> it.getJobProgress(it1) }?.times(100)).toNiceString()
        }.register()

        PlayerPlaceholder(
            this,
            "current_xp"
        ) {
            it.activeJob?.let { it1 -> it.getJobXP(it1) }?.let { it2 -> NumberUtils.format(it2) }
        }.register()

        PlayerPlaceholder(
            this,
            "required_xp"
        ) {
            it.activeJob?.let { it1 -> it.getJobXPRequired(it1).toString() }
        }.register()

        PlayerPlaceholder(
            this,
            "total_job_level"
        ) {
            var level = 0
            for (job in Jobs.values()) {
                level += it.getJobLevel(job)
            }
            level.toString()
        }.register()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcojobs(this),
            CommandJobs(this)
        )
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            JobLevelListener(this),
            JobTriggerXPGainListener,
            ResetOnQuitListener,
            PriceHandler
        )
    }

    override fun loadAdditionalIntegrations(): List<IntegrationLoader> {
        return listOf(
            IntegrationLoader("PlaceholderAPI") { EcoJobsTopExpansion(this).register() }
        )
    }

    companion object {
        @JvmStatic
        lateinit var instance: EcoJobsPlugin
    }
}

