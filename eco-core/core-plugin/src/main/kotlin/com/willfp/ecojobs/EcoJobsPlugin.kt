package com.willfp.ecojobs

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.getActiveJobs
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.api.getJobLimit
import com.willfp.ecojobs.api.jobLimit
import com.willfp.ecojobs.commands.CommandEcoJobs
import com.willfp.ecojobs.commands.CommandJobs
import com.willfp.ecojobs.jobs.JobCategories
import com.willfp.ecojobs.jobs.JobLevelListener
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.JobsGUI
import com.willfp.ecojobs.jobs.PriceHandler
import com.willfp.ecojobs.jobs.ResetOnQuitListener
import com.willfp.ecojobs.libreforge.ConditionHasActiveJob
import com.willfp.ecojobs.libreforge.ConditionHasJobLevel
import com.willfp.ecojobs.libreforge.EffectGiveJobMoney
import com.willfp.ecojobs.libreforge.EffectGiveJobXp
import com.willfp.ecojobs.libreforge.EffectJobMoneyMultiplier
import com.willfp.ecojobs.libreforge.EffectJobXpMultiplier
import com.willfp.ecojobs.libreforge.FilterJob
import com.willfp.ecojobs.libreforge.TriggerGainJobXp
import com.willfp.ecojobs.libreforge.TriggerJoinJob
import com.willfp.ecojobs.libreforge.TriggerLeaveJob
import com.willfp.ecojobs.libreforge.TriggerLevelUpJob
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import wiki.ecohub.config.category.NativeConfigCategory
import wiki.ecohub.core.plugin.EcoPlugin
import wiki.ecohub.data.playerbase.leaderboard.Leaderboards
import wiki.ecohub.data.playerbase.leaderboard.registerCategoryTopPlaceholders
import wiki.ecohub.provider.metrics.EcoMetricsChart
import wiki.ecohub.rules.condition.Conditions
import wiki.ecohub.rules.effect.Effects
import wiki.ecohub.rules.filter.Filters
import wiki.ecohub.rules.holder.SimpleProvidedHolder
import wiki.ecohub.rules.holder.registerSpecificHolderProvider
import wiki.ecohub.rules.trigger.Triggers
import wiki.ecohub.ui.placeholder.type.PlayerDynamicPlaceholder
import wiki.ecohub.ui.placeholder.type.PlayerPlaceholder
import wiki.ecohub.ui.text.formatEco
import java.util.regex.Pattern

internal lateinit var plugin: EcoJobsPlugin
    private set

private val limitPlaceholderPattern = Pattern.compile("limit_.+")

private val inJobsPlaceholderPattern = Pattern.compile("in_jobs_.+")

class EcoJobsPlugin : LibreforgePlugin() {
    init {
        plugin = this
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return listOf(
            JobCategories,
            Jobs
        )
    }

    override fun handleEnable() {
        Conditions.register(ConditionHasJobLevel)
        Conditions.register(ConditionHasActiveJob)
        Effects.register(EffectJobXpMultiplier)
        Effects.register(EffectJobMoneyMultiplier)
        Effects.register(EffectGiveJobXp)
        Effects.register(EffectGiveJobMoney)
        Triggers.register(TriggerGainJobXp)
        Triggers.register(TriggerLevelUpJob)
        Triggers.register(TriggerJoinJob)
        Triggers.register(TriggerLeaveJob)
        Filters.register(FilterJob)

        registerSpecificHolderProvider<Player> { player ->
            if (isDisabledIn(player.world)) emptyList() else
                player.activeJobs.map { it.getLevel(player.getJobLevel(it)) }.map {
                    SimpleProvidedHolder(it)
                }
        }

        // Registered once for every job at once: the lookup resolves the ID when the
        // placeholder is read, so a job added or renamed in a config needs nothing here.
        // A disabled leaderboard is null, so its placeholders stay unresolved until enabled.
        registerCategoryTopPlaceholders(
            this,
            this.langYml.getString("top.empty-position").formatEco(),
            listOf("level", "amount")
        ) { Jobs.getByID(it)?.leaderboard }

        PlayerPlaceholder(
            this,
            "limit"
        ) { it.jobLimit.toString() }.register()

        PlayerPlaceholder(
            this,
            "in_jobs"
        ) { it.getActiveJobs(null).size.toString() }.register()

        PlayerDynamicPlaceholder(this, limitPlaceholderPattern) { args, player ->
            JobCategories.getByID(args.removePrefix("limit_"))?.let { player.getJobLimit(it).toString() }
        }.register()

        PlayerDynamicPlaceholder(this, inJobsPlaceholderPattern) { args, player ->
            JobCategories.getByID(args.removePrefix("in_jobs_"))?.let { player.getActiveJobs(it).size.toString() }
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

    override fun handleReload() {
        // Config categories are loaded in an onReload(START) task, so every Job already exists
        // by the time this runs: unregister first, then re-register from the live jobs.
        Leaderboards.unregisterAll(this)

        for (job in Jobs.values()) {
            job.registerLeaderboard()
        }

        Jobs.registerTally()

        JobsGUI.update()
    }

    override fun loadPluginCommands(): List<PluginCommand> {
        return listOf(
            CommandEcoJobs,
            CommandJobs
        )
    }

    override fun loadListeners(): List<Listener> {
        return listOf(
            JobLevelListener,
            ResetOnQuitListener,
            PriceHandler
        )
    }

    override fun getCustomCharts() = listOf(
        EcoMetricsChart.SingleLine("total_jobs") { Jobs.values().size },
        EcoMetricsChart.SingleLine("job_limit") { configYml.getInt("jobs.limit") },
        EcoMetricsChart.SimplePie("leaderboard_enabled") {
            if (configYml.getBool("leaderboard.enabled")) "enabled" else "disabled"
        },
        EcoMetricsChart.SimplePie("prevent_afk_levelling") {
            if (configYml.getBool("jobs.prevent-levelling-while-afk")) "enabled" else "disabled"
        }
    )
}

