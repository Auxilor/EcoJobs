package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecojobs.api.forceLeaveJob
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.resetJob
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

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
        val jobName = args[1]

        // Reset all jobs for all players
        if (playerName.equals("all", ignoreCase = true) && jobName.equals("all", ignoreCase = true)) {
            Bukkit.getOnlinePlayers().forEach { player ->
                Jobs.values().forEach { job ->
                    if (player.hasJob(job)) {
                        player.forceLeaveJob(job)
                        player.resetJob(job)
                    }
                }
            }
            sender.sendMessage(plugin.langYml.getMessage("reset-all-players-all-jobs"))
            return
        }

        // Reset a specific job for all players
        if (playerName.equals("all", ignoreCase = true)) {
            val job = Jobs.getByID(jobName)
            if (job == null) {
                sender.sendMessage(plugin.langYml.getMessage("invalid-job"))
                return
            }

            Bukkit.getOnlinePlayers().forEach { player ->
                if (player.hasJob(job)) {
                    player.forceLeaveJob(job)
                    player.resetJob(job)
                }
            }

            sender.sendMessage(
                plugin.langYml.getMessage("reset-all-players")
                    .replace("%job%", job.name)
            )
            return
        }

        // Reset all jobs for a specific player
        val player = Bukkit.getPlayer(playerName)
        if (player == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        if (jobName.equals("all", ignoreCase = true)) {
            Jobs.values().forEach { job ->
                if (player.hasJob(job)) {
                    player.forceLeaveJob(job)
                    player.resetJob(job)
                }
            }

            sender.sendMessage(
                plugin.langYml.getMessage("reset-all-jobs")
                    .replace("%player%", player.savedDisplayName)
            )
            return
        }

        // Reset a specific job for a specific player
        val job = Jobs.getByID(jobName)
        if (job == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-job"))
            return
        }

        if (!player.hasJob(job)) {
            sender.sendMessage(plugin.langYml.getMessage("doesnt-have-job"))
            return
        }

        player.forceLeaveJob(job)
        player.resetJob(job)

        sender.sendMessage(
            plugin.langYml.getMessage("reset-xp", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%job%", job.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()
        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                listOf("all") union Bukkit.getOnlinePlayers().map { player -> player.name },
                completions
            )
            return completions
        }

        if (args.size == 2) {
            StringUtil.copyPartialMatches(
                args[1],
                listOf("all") union Jobs.values().map { it.id },
                completions
            )
            return completions
        }

        return emptyList()
    }
}