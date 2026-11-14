package com.willfp.ecojobs.commands

import com.willfp.ecojobs.jobs.Job
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import wiki.ecohub.core.command.CommandBuilder
import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.data.playerbase.leaderboard.Leaderboards
import wiki.ecohub.game.entity.player.PlayerUtils
import wiki.ecohub.ui.placeholder.placeholderContext
import wiki.ecohub.ui.text.formatEco

private val pageCompletions = listOf("1", "2", "3", "4", "5")

private val digits = "\\d+".toRegex()

private const val PAGE_SIZE = 10

/**
 * Reading the leaderboard can block, so the lookup and the reply run async.
 */
internal fun CommandBuilder<CommandSender>.topCommand() = subcommand(
    name = "top",
    permission = "ecojobs.command.top"
) {
    executes {
        val job = Jobs.getByID(args.getOrNull(0))

        if (job == null || !plugin.configYml.getBool("leaderboard.enabled")) {
            fail("invalid-job")
        }

        if (args.getOrNull(1)?.let { it.isNotBlank() && !it.matches(digits) } == true) {
            fail("invalid-page")
        }

        val page = args.getOrNull(1)?.toIntOrNull() ?: 1

        plugin.scheduler.async().run { sendTop(job, page) }
    }

    completes {
        when {
            !plugin.configYml.getBool("leaderboard.enabled") -> emptyList()
            args.size == 1 -> Jobs.values().map { it.id }
            args.size == 2 && Jobs.getByID(args[0]) != null -> pageCompletions
            else -> emptyList()
        }
    }
}

private fun CommandContext<CommandSender>.sendTop(job: Job, page: Int) {
    val offset = (page - 1) * PAGE_SIZE
    val top = (offset + 1..offset + PAGE_SIZE).mapNotNull { job.leaderboard?.getTop(it) }

    val lines = top.map { entry ->
        plugin.langYml.getString("top-line-format")
            .replace(oldValue = "%rank%", newValue = entry.position.toString())
            .replace(oldValue = "%level%", newValue = entry.value.toInt().toString())
            .replace(oldValue = "%player%", newValue = PlayerUtils.getSavedDisplayName(entry.player))
    }.ifEmpty { listOf(Leaderboards.getNoRecordsMessage(plugin)) }

    val messages = plugin.langYml.getStrings("top.format").toMutableList()
    val linesIndex = messages.indexOf("%lines%")

    if (linesIndex != -1) {
        messages.removeAt(linesIndex)
        messages.addAll(linesIndex, lines)
    }

    val context = placeholderContext(player = sender as? Player)

    messages.forEach { sender.sendMessage(it.formatEco(context)) }
}
