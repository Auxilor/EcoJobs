package com.willfp.ecojobs.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.plugin
import com.willfp.ecojobs.runGlobal
import org.bukkit.command.CommandSender

object CommandReload : Subcommand(
    plugin,
    "reload",
    "ecojobs.command.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        runGlobal {
            sender.sendMessage(
                plugin.langYml.getMessage("reloaded", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                    .replace("%time%", plugin.reloadWithTime().toNiceString())
                    .replace("%count%", Jobs.values().size.toString())
            )
        }
    }
}