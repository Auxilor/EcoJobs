package com.willfp.ecojobs.commands

import com.willfp.ecojobs.api.forceLeaveJob
import com.willfp.ecojobs.api.hasJob
import com.willfp.ecojobs.api.resetJob
import com.willfp.ecojobs.jobs.Job
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.runOwnedIfOnline
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.data.playerbase.forEachSavedProfile
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.game.entity.runOwned
import java.util.logging.Level

internal fun CommandBuilder<CommandSender>.resetCommand() = subcommand(
    name = "reset",
    permission = "ecojobs.command.reset"
) {
    executes {
        val playerName = argument(0, "needs-player")
        val jobName = argument(1, "needs-job")
        val allPlayers = playerName.equals("all", ignoreCase = true)
        val allJobs = jobName.equals("all", ignoreCase = true)

        when {
            allPlayers && allJobs -> {
                resetAllPlayers(Jobs.values(), plugin.langYml.getMessage("reset-all-players-all-jobs"))
            }

            allPlayers -> {
                val job = Jobs.getByID(jobName) ?: fail("invalid-job")
                resetAllPlayers(
                    listOf(job),
                    plugin.langYml.getMessage("reset-all-players").replace(oldValue = "%job%", newValue = job.name)
                )
            }

            else -> {
                resetPlayer(Bukkit.getPlayer(playerName) ?: fail("invalid-player"), jobName, allJobs)
            }
        }
    }

    completes {
        when (args.size) {
            1 -> listOf("all") + playerNames()
            2 -> listOf("all") + Jobs.values().map { it.id }
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.resetPlayer(player: Player, jobName: String, allJobs: Boolean) {
    if (allJobs) {
        player.runOwned {
            resetJobs(player, Jobs.values())
            reply("reset-all-jobs", "player" to PlayerUtils.getSavedDisplayName(player))
        }
        return
    }

    val job = Jobs.getByID(jobName) ?: fail("invalid-job")

    if (!player.hasJob(job)) {
        fail("doesnt-have-job")
    }

    player.runOwned {
        player.forceLeaveJob(job)
        player.resetJob(job)

        replyWithoutPlaceholders(
            "reset-xp",
            "player" to PlayerUtils.getSavedDisplayName(player),
            "job" to job.name
        )
    }
}

private fun CommandContext<CommandSender>.resetAllPlayers(jobs: Collection<Job>, message: String) {
    forEachSavedProfile(plugin) { uuid ->
        val player = Bukkit.getOfflinePlayer(uuid)

        player.runOwnedIfOnline { resetJobs(player, jobs) }
    }.thenRun {
        plugin.scheduler.global().run { sender.sendMessage(message) }
    }.exceptionally { error ->
        plugin.logger.log(Level.SEVERE, "Failed to reset jobs for every player", error)
        null
    }
}

private fun resetJobs(player: OfflinePlayer, jobs: Collection<Job>) {
    for (job in jobs) {
        if (player.hasJob(job)) {
            player.forceLeaveJob(job)
            player.resetJob(job)
        }
    }
}
