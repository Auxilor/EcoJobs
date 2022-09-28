package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.ecojobs.jobs.activeJob
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandLeave(plugin: EcoPlugin) : Subcommand(plugin, "leave", "ecojobs.command.leave", true) {
    override fun onExecute(player: CommandSender, args: List<String>) {
        player as Player

        if (player.activeJob == null) {
            player.sendMessage(plugin.langYml.getMessage("no-job"))
            return
        }

        player.sendMessage(
            plugin.langYml.getMessage("left-job", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%job%", player.activeJob?.name ?: "")
        )

        player.activeJob = null
    }
}
