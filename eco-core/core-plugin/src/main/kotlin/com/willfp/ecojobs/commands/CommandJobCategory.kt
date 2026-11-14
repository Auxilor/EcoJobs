package com.willfp.ecojobs.commands

import com.willfp.ecojobs.jobs.JobCategory
import com.willfp.ecojobs.plugin
import wiki.ecohub.core.command.EcoCommand
import wiki.ecohub.core.command.playerCommand

internal fun jobCategoryCommand(category: JobCategory, name: String): EcoCommand =
    plugin.playerCommand(name = name, permission = "ecojobs.command.jobs") {
        executes {
            category.open(sender)
        }
    }
