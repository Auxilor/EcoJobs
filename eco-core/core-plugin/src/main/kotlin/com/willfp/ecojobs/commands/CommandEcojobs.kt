package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender

class CommandEcojobs(plugin: EcoPlugin) : PluginCommand(plugin, "ecojobs", "ecojobs.command.ecojobs", false) {
    init {
        this.addSubcommand(CommandReload(plugin))
            .addSubcommand(CommandGive(plugin))
            .addSubcommand(CommandGiveXP(plugin))
            .addSubcommand(CommandReset(plugin))
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }
}