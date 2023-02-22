package com.willfp.ecojobs

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.util.toSingletonList
import com.willfp.ecojobs.commands.CommandEcojobs
import com.willfp.ecojobs.commands.CommandJobs
import com.willfp.ecojobs.jobs.*
import com.willfp.libreforge.LibReforgePlugin
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable

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
            "job_level_next"
        ) { it.activeJobLevel?.level?.let { l -> (l + 1).toString() } ?: "" }.register()

        PlayerPlaceholder(
            this,
            "job_percent"
        ) { p -> p.activeJob?.let { "${String.format("%.1f", p.getJobProgress(it) * 100)}%" } ?: "" }.register()

        PlayerPlaceholder(
            this,
            "job_id"
        ) { it.activeJob?.id ?: "" }.register()

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

    val jobBlockListener = JobBlockBreakListener(this)

    override fun loadListeners(): List<Listener> {
        return listOf(
            JobLevelListener(this),
            JobTriggerXPGainListener,
            ResetOnQuitListener,
            PriceHandler,
            jobBlockListener
        )
    }

    override fun handleReloadAdditional() {
        super.handleReloadAdditional()
        runnableFactory.create {
            jobBlockListener.processQueue()
        }.runTaskTimer(0, 2)
    }

    companion object {
        @JvmStatic
        lateinit var instance: EcoJobsPlugin
    }
}

