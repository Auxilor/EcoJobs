package com.willfp.ecojobs.commands

import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.setJobLevel
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.runOwnedIfOnline
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.game.entity.player.PlayerUtils

internal fun CommandBuilder<CommandSender>.unlockCommand() = subcommand(
    name = "unlock",
    permission = "ecojobs.command.unlock"
) {
    executes {
        val playerName = argument(0, "needs-player")
        val jobId = argument(1, "needs-job")

        @Suppress("DEPRECATION")
        val player = Bukkit.getPlayer(playerName) ?: Bukkit.getOfflinePlayer(playerName)

        if (!player.isOnline && !player.hasPlayedBefore()) {
            fail("invalid-player")
        }

        val job = Jobs.getByID(jobId) ?: fail("invalid-job")

        if (player.hasJob(job)) {
            fail("already-has-job")
        }

        player.runOwnedIfOnline {
            player.setJobLevel(job, 1)

            replyWithoutPlaceholders(
                "unlocked-job",
                "player" to PlayerUtils.getSavedDisplayName(player),
                "job" to job.name
            )
        }
    }

    completes {
        when (args.size) {
            1 -> playerNames()
            2 -> Jobs.values().map { it.id }
            else -> emptyList()
        }
    }
}
