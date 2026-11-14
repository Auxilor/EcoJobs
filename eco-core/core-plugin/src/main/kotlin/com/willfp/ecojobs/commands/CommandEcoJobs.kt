package com.willfp.ecojobs.commands

import com.willfp.ecojobs.EcoJobsPlugin
import wiki.ecohub.core.command.command

internal fun EcoJobsPlugin.ecoJobsCommand() = command(name = "ecojobs", permission = "ecojobs.command.ecojobs") {
    reloadCommand()
    unlockCommand()
    giveXPCommand()
    resetCommand()
}
