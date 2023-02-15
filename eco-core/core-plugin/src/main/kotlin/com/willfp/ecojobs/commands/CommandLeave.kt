package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.leaveJob
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandLeave(plugin: EcoPlugin) : Subcommand(plugin, "leave", "ecojobs.command.leave", true) {
    override fun onExecute(player: CommandSender, args: List<String>) {
        player as Player

        if (args.isEmpty()) {
            player.sendMessage(plugin.langYml.getMessage("needs-job"))
            return
        }

        if (player.activeJobs.isEmpty()) {
            player.sendMessage(plugin.langYml.getMessage("no-job"))
            return
        }

        val id = args[0]

        val job = Jobs.getByID(id)

        if (job == null || !player.hasJob(job)) {
            player.sendMessage(plugin.langYml.getMessage("invalid-job"))
            return
        }

        if (!player.hasJobActive(job)) {
            player.sendMessage(plugin.langYml.getMessage("not-in-job"))
            return
        }

        player.leaveJob(job)

        if (!player.hasJobActive(job)) {
            player.sendMessage(
                plugin.langYml.getMessage("left-job")
                    .replace("%job%", job.name)
            )
        } else {
            player.sendMessage(
                plugin.langYml.getMessage("cant-leave-job")
                    .replace("%job%", job.name)
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        if (sender !is Player) {
            return emptyList()
        }

        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            return Jobs.values().filter { sender.hasJobActive(it) }.map { it.id }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Jobs.values().filter { sender.hasJobActive(it) }.map { it.id },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
