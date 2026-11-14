package com.willfp.ecojobs.commands

import com.willfp.ecojobs.api.giveExactJobExperience
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.stdlib.toNiceString
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.game.entity.runOwned

private val xpCompletions = listOf("10", "100", "1000", "10000")

private const val AMOUNT_ARGS = 3

internal fun CommandBuilder<CommandSender>.giveXPCommand() = subcommand(
    name = "givexp",
    permission = "ecojobs.command.givexp"
) {
    executes {
        val playerName = argument(0, "needs-player")
        val jobId = argument(1, "needs-job")
        val amountArg = argument(2, "needs-amount")

        val player = Bukkit.getPlayer(playerName) ?: fail("invalid-player")
        val job = Jobs.getByID(jobId) ?: fail("invalid-job")

        if (!player.hasJob(job)) {
            fail("doesnt-have-job")
        }

        if (!player.hasJobActive(job)) {
            fail("player-not-in-job")
        }

        val amount = amountArg.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 } ?: fail("invalid-amount")

        player.runOwned {
            player.giveExactJobExperience(job, amount)

            replyWithoutPlaceholders(
                "gave-xp",
                "player" to PlayerUtils.getSavedDisplayName(player),
                "xp" to amount.toNiceString(),
                "job" to job.name
            )
        }
    }

    completes {
        when (args.size) {
            1 -> playerNames()
            2 -> Jobs.values().map { it.id }
            AMOUNT_ARGS -> xpCompletions
            else -> emptyList()
        }
    }
}
