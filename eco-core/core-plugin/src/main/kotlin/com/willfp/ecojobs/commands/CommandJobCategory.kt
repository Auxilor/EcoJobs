package com.willfp.ecojobs.commands

import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.ecojobs.jobs.JobCategory
import com.willfp.ecojobs.plugin
import org.bukkit.entity.Player

class CommandJobCategory(
    private val category: JobCategory,
    name: String
) : PluginCommand(
    plugin,
    name,
    "ecojobs.command.jobs",
    true
) {
    override fun onExecute(player: Player, args: List<String>) {
        category.open(player)
    }
}
