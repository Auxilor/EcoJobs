package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecojobs.jobs.JobsGUI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandJobs(plugin: EcoPlugin) : PluginCommand(plugin, "jobs", "ecojobs.command.jobs", true) {
    init {
        this.addSubcommand(CommandJoin(plugin))
            .addSubcommand(CommandLeave(plugin))
    }

    override fun onExecute(player: CommandSender, args: List<String>) {
        player as Player
        JobsGUI.open(player)
    }
}
