package com.willfp.ecojobs

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.placeholder.DynamicPlaceholder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.api.jobLimit
import com.willfp.ecojobs.commands.CommandEcoJobs
import com.willfp.ecojobs.commands.CommandJobs
import com.willfp.ecojobs.jobs.EcoJobsJobTopPlaceholder
import com.willfp.ecojobs.jobs.JobLevelListener
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.JobsGUI
import com.willfp.ecojobs.jobs.PriceHandler
import com.willfp.ecojobs.jobs.ResetOnQuitListener
import com.willfp.ecojobs.libreforge.ConditionHasActiveJob
import com.willfp.ecojobs.libreforge.ConditionHasJobLevel
import com.willfp.ecojobs.libreforge.EffectGiveJobXp
import com.willfp.ecojobs.libreforge.EffectJobXpMultiplier
import com.willfp.ecojobs.libreforge.FilterJob
import com.willfp.ecojobs.libreforge.TriggerGainJobXp
import com.willfp.ecojobs.libreforge.TriggerJoinJob
import com.willfp.ecojobs.libreforge.TriggerLeaveJob
import com.willfp.ecojobs.libreforge.TriggerLevelUpJob
import com.willfp.libreforge.SimpleProvidedHolder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.filters.Filters
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.registerHolderProvider
import com.willfp.libreforge.registerSpecificHolderProvider
import com.willfp.libreforge.triggers.Triggers
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.regex.Pattern

class EcoJobsPlugin : LibreforgePlugin() {
    init {
        instance = this
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return listOf(
            Jobs
        )
    }

    override fun handleEnable() {
        Conditions.register(ConditionHasJobLevel)
        Conditions.register(ConditionHasActiveJob)
        Effects.register(EffectJobXpMultiplier)
        Effects.register(EffectGiveJobXp)
        Triggers.register(TriggerGainJobXp)
        Triggers.register(TriggerLevelUpJob)
        Triggers.register(TriggerJoinJob)
        Triggers.register(TriggerLeaveJob)
        Filters.register(FilterJob)
        
        registerSpecificHolderProvider<Player> { player ->
            player.activeJobs.map { it.getLevel(player.getJobLevel(it)) }.map {
                SimpleProvidedHolder(it)
            }
        }

        EcoJobsJobTopPlaceholder(this).register()

        PlayerPlaceholder(
            this,
            "limit"
        ) { it.jobLimit.toString() }.register()

        PlayerPlaceholder(
            this,
            "in_jobs"
        ) { it.activeJobs.size.toString() }.register()

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

    override fun handleReload() {
        JobsGUI.update(this)
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoJobs(this),
            CommandJobs(this)
        )
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            JobLevelListener(this),
            ResetOnQuitListener,
            PriceHandler
        )
    }

    companion object {
        @JvmStatic
        lateinit var instance: EcoJobsPlugin
    }
}

