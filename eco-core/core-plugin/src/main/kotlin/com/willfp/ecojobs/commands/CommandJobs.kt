package com.willfp.ecojobs.commands

import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.JobsGUI
import org.bukkit.entity.Player
import wiki.ecohub.core.command.command

/**
 * Any sender can run `top`; the GUI and the other subcommands need a player.
 */
internal fun EcoJobsPlugin.jobsCommand() = command(name = "jobs", permission = "ecojobs.command.jobs") {
    joinCommand()
    leaveCommand()
    topCommand()

    executes {
        val player = sender as? Player ?: fail("not-player")
        val job = args.firstOrNull()?.let { Jobs.getByID(it.lowercase()) ?: fail("invalid-job") }

        if (job == null) {
            JobsGUI.open(player)
        } else {
            if (!player.hasJob(job)) {
                fail("dont-have-job")
            }

            job.levelGUI.open(player)
        }
    }

    completes {
        val player = sender as? Player

        if (player == null || args.size != 1) {
            emptyList()
        } else {
            Jobs.values().filter { player.hasJob(it) }.map { it.id }
        }
    }
}
