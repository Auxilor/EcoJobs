package com.willfp.ecojobs.commands

import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.leaveJob
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

internal fun CommandBuilder<CommandSender>.leaveCommand() = playerSubcommand(
    name = "leave",
    permission = "ecojobs.command.leave"
) {
    executes {
        val id = argument(0, "needs-job")

        if (sender.activeJobs.isEmpty()) {
            fail("no-job")
        }

        val job = Jobs.getByID(id)?.takeIf { sender.hasJob(it) } ?: fail("invalid-job")

        if (!sender.hasJobActive(job)) {
            fail("not-in-job")
        }

        sender.leaveJob(job)

        if (sender.hasJobActive(job)) {
            reply("cant-leave-job", "job" to job.name)
        } else {
            reply("left-job", "job" to job.name)
        }
    }

    completes {
        if (args.size == 1) {
            Jobs.values().filter { sender.hasJobActive(it) }.map { it.id }
        } else {
            emptyList()
        }
    }
}
