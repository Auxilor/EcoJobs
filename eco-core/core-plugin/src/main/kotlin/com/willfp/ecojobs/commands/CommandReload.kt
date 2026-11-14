package com.willfp.ecojobs.commands

import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.platform.runGlobal
import wiki.ecohub.core.stdlib.toNiceString

/**
 * Reloading rebuilds shared registries, so it runs on the global region.
 */
internal fun CommandBuilder<CommandSender>.reloadCommand() = subcommand(
    name = "reload",
    permission = "ecojobs.command.reload"
) {
    executes {
        Bukkit.getServer().runGlobal {
            replyWithoutPlaceholders(
                "reloaded",
                "time" to plugin.reloadWithTime().toNiceString(),
                "count" to Jobs.values().size.toString()
            )
        }
    }
}
