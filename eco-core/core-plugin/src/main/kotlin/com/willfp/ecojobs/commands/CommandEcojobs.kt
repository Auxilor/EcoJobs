package com.willfp.ecojobs.commands

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.libreforge.LibReforgePlugin
import com.willfp.libreforge.lrcdb.CommandExport
import com.willfp.libreforge.lrcdb.CommandImport
import com.willfp.libreforge.lrcdb.ExportableConfig
import org.bukkit.command.CommandSender

class CommandEcojobs(plugin: LibReforgePlugin) : PluginCommand(plugin, "ecojobs", "ecojobs.command.ecojobs", false) {
    init {
        this.addSubcommand(CommandReload(plugin))
            .addSubcommand(CommandUnlock(plugin))
            .addSubcommand(CommandGiveXP(plugin))
            .addSubcommand(CommandReset(plugin))
            .addSubcommand(CommandImport("jobs", plugin))
            .addSubcommand(CommandExport(plugin) {
                Jobs.values().map {
                    ExportableConfig(
                        it.id,
                        it.config
                    )
                }
            })
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }
}
