package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.activeJob
import com.willfp.ecojobs.jobs.hasJob
import com.willfp.ecojobs.jobs.resetJob
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class CommandReset(plugin: EcoPlugin) : Subcommand(plugin, "reset", "ecojobs.command.reset", false) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("needs-player"))
            return
        }

        if (args.size == 1) {
            sender.sendMessage(plugin.langYml.getMessage("needs-job"))
            return
        }

        val playerName = args[0]

        val player = Bukkit.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        val job = Jobs.getByID(args[1])

        if (job == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-job"))
            return
        }

        if (!player.hasJob(job)) {
            sender.sendMessage(plugin.langYml.getMessage("doesnt-have-job"))
            return
        }

        if (player.activeJob == job) {
            player.activeJob = null
        }
        player.resetJob(job)

        sender.sendMessage(
            plugin.langYml.getMessage("reset-xp", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%job%", job.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        if (args.size == 1) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 2) {
            return Jobs.values().map { it.id }
        }

        return emptyList()
    }
}