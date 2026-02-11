package com.willfp.ecojobs.jobs

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.plugin
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.time.Duration
import java.util.UUID

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

    fun Job.getTop(position: Int): LeaderboardEntry? {
        require(position > 0) { "Position must be greater than 0" }

        val uuid = leaderboardCache.get(true)[this]?.getOrNull(position - 1) ?: return null

        val player = Bukkit.getOfflinePlayer(uuid).takeIf { it.hasPlayedBefore() } ?: return null

        return LeaderboardEntry(
            player,
            player.getJobLevel(this)
        )
    }

    fun Job.getPosition(uuid: UUID): Int? {
        val leaderboard = leaderboardCache.get(true)[this]
        val index = leaderboard?.indexOf(uuid)
        return if (index == -1) null else index?.plus(1)
    }

    data class LeaderboardEntry(
        val player: OfflinePlayer,
        val level: Int
    )
}