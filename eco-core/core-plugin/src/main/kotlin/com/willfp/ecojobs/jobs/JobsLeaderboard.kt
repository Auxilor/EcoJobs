package com.willfp.ecojobs.jobs

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.tuples.Pair
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.util.LeaderboardCacheEntry
import org.bukkit.Bukkit
import java.time.Duration
import java.util.*

object JobsLeaderboard {
    private var topLeaderboard = Caffeine.newBuilder()
        .expireAfterWrite(
            Duration.ofSeconds(
                EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong()
            )
        )
        .build<Boolean, Map<TopEntry, LeaderboardCacheEntry?>> {
            val offlinePlayers = Bukkit.getOfflinePlayers()
            val top = mutableMapOf<TopEntry, LeaderboardCacheEntry>()
            for (job in Jobs.values())
                top.putAll(offlinePlayers.sortedByDescending { it.getJobLevel(job) }
                    .mapIndexed { place, player ->
                        TopEntry(job, place + 1) to LeaderboardCacheEntry(
                            player,
                            player.getJobLevel(job)
                        )
                    })
            return@build top
        }
    private var posLeaderboard = Caffeine.newBuilder()
        .expireAfterWrite(
            Duration.ofSeconds(
                EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong()
            )
        )
        .build<Boolean, Map<PosEntry, Int>> {
            val offlinePlayers = Bukkit.getOfflinePlayers()
            val pos = mutableMapOf<PosEntry, Int>()
            for (job in Jobs.values())
                pos.putAll(offlinePlayers.sortedByDescending { it.getJobLevel(job) }
                    .map { PosEntry(job, it.uniqueId) to it.getJobLevel(job) })
            return@build pos
        }

    fun getTop(job: Job, place: Int): LeaderboardCacheEntry? {
        return topLeaderboard.get(true)[TopEntry(job, place)]
    }

    fun getPosition(job: Job, uuid: UUID): Int? {
        return posLeaderboard.get(true)[PosEntry(job, uuid)]?.plus(1)
    }

    private class TopEntry(val job: Job, val place: Int) {
        override fun equals(other: Any?): Boolean {
            if (other !is TopEntry) return false
            return job == other.job && place == other.place
        }

        override fun hashCode(): Int {
            return Objects.hash(job, place)
        }
    }

    private class PosEntry(val job: Job, val uuid: UUID) {

        override fun equals(other: Any?): Boolean {
            if (other !is PosEntry) return false
            return job == other.job && uuid == other.uuid
        }

        override fun hashCode(): Int {
            return Objects.hash(job, uuid)
        }
    }
}