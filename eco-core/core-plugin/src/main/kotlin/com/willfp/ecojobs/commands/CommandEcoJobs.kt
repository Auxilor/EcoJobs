package com.willfp.ecojobs.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecojobs.plugin
import org.bukkit.command.CommandSender

object CommandEcoJobs : PluginCommand(
    plugin,
    "ecojobs",
    "ecojobs.command.ecojobs",
    false
) {
    init {
        this.addSubcommand(CommandReload)
            .addSubcommand(CommandUnlock)
            .addSubcommand(CommandGiveXP)
            .addSubcommand(CommandReset)
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }
}
