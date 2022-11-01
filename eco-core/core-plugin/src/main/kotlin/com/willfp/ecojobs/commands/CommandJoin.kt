package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.activeJob
import com.willfp.ecojobs.jobs.hasJob
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandJoin(plugin: EcoPlugin) : Subcommand(plugin, "join", "ecojobs.command.join", true) {
    override fun onExecute(player: CommandSender, args: List<String>) {
        player as Player

        if (args.isEmpty()) {
            player.sendMessage(plugin.langYml.getMessage("needs-job"))
            return
        }

        val id = args[0]

        val job = Jobs.getByID(id)

        if (job == null || !player.hasJob(job)) {
            player.sendMessage(plugin.langYml.getMessage("invalid-job"))
            return
        }

        if (player.activeJob == job) {
            player.sendMessage(plugin.langYml.getMessage("job-already-joined"))
            return
        }

        player.sendMessage(
            plugin.langYml.getMessage("joined-job", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%job%", job.name)
        )
        player.activeJob = job
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        if (sender !is Player) {
            return emptyList()
        }

        val completions = mutableListOf<String>()
        if (args.isEmpty()) {
            // Currently, this case is not ever reached
            return Jobs.values().filter { sender.hasJob(it) }.map { it.id }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Jobs.values().filter { sender.hasJob(it) }.map { it.id },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
