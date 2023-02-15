package com.willfp.ecojobs

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.api.jobLimit
import com.willfp.ecojobs.commands.CommandEcojobs
import com.willfp.ecojobs.commands.CommandJobs
import com.willfp.ecojobs.jobs.JobLevelListener
import com.willfp.ecojobs.jobs.JobTriggerXPGainListener
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.PriceHandler
import com.willfp.ecojobs.jobs.ResetOnQuitListener
import com.willfp.libreforge.LibReforgePlugin
import org.bukkit.event.Listener

class EcoJobsPlugin : LibReforgePlugin() {
    init {
        instance = this
        registerHolderProvider { player ->
            player.activeJobs.map { it.getLevel(player.getJobLevel(it)) }
        }
    }

    override fun handleEnableAdditional() {
        this.copyConfigs("jobs")

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

    companion object {
        @JvmStatic
        lateinit var instance: EcoJobsPlugin
    }
}

