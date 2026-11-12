package com.willfp.ecojobs.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.plugin
import com.willfp.ecojobs.runGlobal
import org.bukkit.command.CommandSender
import wiki.ecohub.core.stdlib.toNiceString
import wiki.ecohub.ui.text.StringUtils

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