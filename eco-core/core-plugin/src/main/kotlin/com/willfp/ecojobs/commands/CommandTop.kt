package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.core.placeholder.context.placeholderContext
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.JobsLeaderboard.getTop
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class CommandTop(plugin: EcoPlugin) : Subcommand(plugin, "top", "ecojobs.command.top", false) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        plugin.scheduler.runAsync {
            val job = Jobs.getByID(args.getOrNull(0))

            if (job == null) {
                sender.sendMessage(plugin.langYml.getMessage("invalid-job"))
                return@runAsync
            }

            val page = args.getOrNull(1)?.toIntOrNull() ?: 1

            if (args.getOrNull(1)?.let { it.isNotBlank() && !it.matches("\\d+".toRegex()) } == true) {
                sender.sendMessage(plugin.langYml.getMessage("invalid-page"))
                return@runAsync
            }

            val offset = (page - 1) * 10
            val positions = (offset + 1..offset + 10).toList()

            val top = positions.mapNotNull { getTop(job, it) }

            val messages = plugin.langYml.getStrings("top.format").toMutableList()
            val lines = mutableListOf<String>()

            top.forEachIndexed { index, entry ->
                val line = plugin.langYml.getString("top-line-format")
                    .replace("%rank%", (offset + index + 1).toString())
                    .replace("%level%", entry.level.toString())
                    .replace("%player%", entry.player.savedDisplayName)
                lines.add(line)
            }

            val linesIndex = messages.indexOf("%lines%")
            if (linesIndex != -1) {
                messages.removeAt(linesIndex)
                messages.addAll(linesIndex, lines)
            }

            messages.forEach { message ->
                sender.sendMessage(
                    message.formatEco(
                        placeholderContext(
                            player = sender as? Player
                        )
                    )
                )
            }
        }
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Jobs.values().map { it.id },
                completions
            )
            return completions
        }

        if (args.size == 2 && Jobs.getByID(args[0]) != null) {
            StringUtil.copyPartialMatches(
                args[1],
                listOf("1", "2", "3", "4", "5"),
                completions
            )
            return completions
        }

        return emptyList()
    }
}