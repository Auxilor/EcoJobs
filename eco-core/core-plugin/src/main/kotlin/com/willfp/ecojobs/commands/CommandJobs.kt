package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.JobsGUI
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandJobs(plugin: EcoPlugin) : PluginCommand(plugin, "jobs", "ecojobs.command.jobs", true) {
    init {
        this.addSubcommand(CommandJoin(plugin))
            .addSubcommand(CommandLeave(plugin))
        if (plugin.configYml.getBool("leaderboard.enabled"))
            this.addSubcommand(CommandTop(plugin))
    }

    override fun onExecute(player: Player, args: List<String>) {
        if (args.isEmpty()) {
            JobsGUI.open(player)
            return
        }

        val id = args[0].lowercase()
        val job = Jobs.getByID(id)

        if (job == null) {
            player.sendMessage(plugin.langYml.getMessage("invalid-job"))
            return
        }

        if (!player.hasJob(job)) {
            player.sendMessage(plugin.langYml.getMessage("dont-have-job"))
            return
        }

        job.levelGUI.open(player)
    }

    override fun tabComplete(player: Player, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            return Jobs.values().filter { player.hasJob(it) }.map { it.id }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Jobs.values().filter { player.hasJob(it) }.map { it.id },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
