package com.willfp.ecojobs.commands

import com.willfp.ecojobs.api.canJoinJob
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.joinJob
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder

internal fun CommandBuilder<CommandSender>.joinCommand() = playerSubcommand(
    name = "join",
    permission = "ecojobs.command.join"
) {
    executes {
        val id = argument(0, "needs-job")
        val job = Jobs.getByID(id)?.takeIf { sender.hasJob(it) } ?: fail("invalid-job")

        if (sender.hasJobActive(job)) {
            fail("job-already-joined")
        }

        if (!sender.canJoinJob(job)) {
            fail("cannot-join-job")
        }

        sender.joinJob(job)

        if (sender.hasJobActive(job)) {
            replyWithoutPlaceholders("joined-job", "job" to job.name)
        }
    }

    completes {
        if (args.size == 1) {
            Jobs.values().filter { sender.hasJob(it) }.map { it.id }
        } else {
            emptyList()
        }
    }
}
