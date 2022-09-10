package com.willfp.ecojobs

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.util.toSingletonList
import com.willfp.ecojobs.commands.CommandEcojobs
import com.willfp.ecojobs.commands.CommandJobs
import com.willfp.ecojobs.jobs.JobLevelListener
import com.willfp.ecojobs.jobs.JobTriggerXPGainListener
import com.willfp.ecojobs.jobs.activeJob
import com.willfp.ecojobs.jobs.activeJobLevel
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
            "job_id"
        ) { it.activeJob?.id ?: "" }.register()
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
            JobTriggerXPGainListener
        )
    }

    companion object {
        @JvmStatic
        lateinit var instance: EcoJobsPlugin
    }
}

