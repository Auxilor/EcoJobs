package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.setJobLevel
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

class CommandUnlock(plugin: EcoPlugin) : Subcommand(plugin, "unlock", "ecojobs.command.unlock", false) {
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

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(playerName)

        if (!player.hasPlayedBefore()) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        val job = Jobs.getByID(args[1])

        if (job == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-job"))
            return
        }

        if (player.hasJob(job)) {
            sender.sendMessage(plugin.langYml.getMessage("already-has-job"))
            return
        }

        player.setJobLevel(job, 1)
        sender.sendMessage(
            plugin.langYml.getMessage("unlocked-job", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%job%", job.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()
        if (args.isEmpty()) {
            // Currently, this case is not ever reached
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                completions
            )
            return completions
        }

        if (args.size == 2) {
            StringUtil.copyPartialMatches(
                args[1],
                Jobs.values().map { it.id },
                completions
            )
            return completions
        }

        return emptyList()
    }
}
