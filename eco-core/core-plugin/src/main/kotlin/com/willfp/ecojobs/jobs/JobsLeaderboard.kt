package com.willfp.ecojobs.jobs

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.tuples.Pair
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.plugin
import com.willfp.ecojobs.util.LeaderboardEntry
import org.bukkit.Bukkit
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

object JobsLeaderboard {
    private var leaderboardCache = Caffeine.newBuilder()
        .expireAfterWrite(
            Duration.ofSeconds(
                plugin.configYml.getInt("leaderboard.cache-lifetime").toLong()
            )
        )
        .build<Boolean, Map<Job, List<UUID>>> {
            if (!plugin.configYml.getBool("leaderboard.enabled"))
                return@build emptyMap()
            val offlinePlayers = Bukkit.getOfflinePlayers()
            val top = mutableMapOf<Job, List<UUID>>()
            for (job in Jobs.values())
                top[job] = offlinePlayers.sortedByDescending { it.getJobLevel(job) }.map { it.uniqueId }
            return@build top
        }

    fun getTop(job: Job, position: Int): LeaderboardEntry? {
        require(position > 0) { "Position must be greater than 0" }

        val uuid = leaderboardCache.get(true)[job]?.getOrNull(position - 1) ?: return null

        val player = Bukkit.getOfflinePlayer(uuid).takeIf { it.hasPlayedBefore() } ?: return null

        return LeaderboardEntry(
            player,
            player.getJobLevel(job)
        )
    }

    fun getPosition(job: Job, uuid: UUID): Int? {
        val leaderboard = leaderboardCache.get(true)[job]
        val index = leaderboard?.indexOf(uuid)
        return if (index == -1) null else index?.plus(1)
    }
}