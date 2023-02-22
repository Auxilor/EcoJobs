package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import com.willfp.ecojobs.api.giveExactJobExperience
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class CommandGiveXP(plugin: EcoPlugin) : Subcommand(plugin, "givexp", "ecojobs.command.givexp", false) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("needs-player"))
            return
        }

        if (args.size == 1) {
            sender.sendMessage(plugin.langYml.getMessage("needs-job"))
            return
        }

        if (args.size == 2) {
            sender.sendMessage(plugin.langYml.getMessage("needs-amount"))
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

        val amount = args[2].toDoubleOrNull()

        if (amount == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        player.giveExactJobExperience(
            job,
            amount
        )

        sender.sendMessage(
            plugin.langYml.getMessage("gave-xp", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%xp%", amount.toNiceString())
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

        if (args.size == 3) {
            return listOf("10", "100", "1000", "10000")
        }

        return emptyList()
    }
}