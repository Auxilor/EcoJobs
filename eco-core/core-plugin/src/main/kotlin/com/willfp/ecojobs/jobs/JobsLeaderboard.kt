package com.willfp.ecojobs.jobs

import com.willfp.eco.core.tuples.Pair
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.util.LeaderboardCacheEntry
import org.bukkit.Bukkit
import java.time.Duration
import java.util.*

object JobsLeaderboard {
    private val topCache = mutableMapOf<Pair<Job, Int>, LeaderboardCacheEntry>()
    private var topCacheLastUpdate: Long = System.currentTimeMillis()
    private var topCacheNextUpdate: Long =
        System.currentTimeMillis() + Duration.ofSeconds(
            EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong()
        ).toMillis()
    private val posCache = mutableMapOf<Pair<Job, UUID>, Int>()
    private var posCacheLastUpdate: Long = System.currentTimeMillis()
    private var posCacheNextUpdate: Long =
        System.currentTimeMillis() + Duration.ofSeconds(
            EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong()
        ).toMillis()

    fun getTop(job: Job, place: Int): LeaderboardCacheEntry? {
        if (topCacheNextUpdate <= topCacheLastUpdate +
            Duration.ofSeconds(EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong())
                .toMillis()
        ) {
            topCacheLastUpdate = System.currentTimeMillis()
            topCacheNextUpdate = topCacheLastUpdate +
                    Duration.ofSeconds(EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong())
                        .toMillis()
            topCache.clear()
            val offlinePlayers = Bukkit.getOfflinePlayers()
            for (job in Jobs.values())
                topCache.putAll(offlinePlayers.sortedByDescending { it.getJobLevel(job) }
                    .mapIndexed { place, player ->
                        Pair(job, place + 1) to LeaderboardCacheEntry(
                            player,
                            player.getJobLevel(job)
                        )
                    })

        }
        return topCache[Pair(job, place)]
    }

    fun getPosition(job: Job, uuid: UUID): Int? {
        if (posCacheNextUpdate <= posCacheLastUpdate +
            Duration.ofSeconds(EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong())
                .toMillis()
        ) {
            posCacheLastUpdate = System.currentTimeMillis()
            posCacheNextUpdate = posCacheLastUpdate +
                    Duration.ofSeconds(EcoJobsPlugin.instance.configYml.getInt("leaderboard-cache-lifetime").toLong())
                        .toMillis()
            posCache.clear()
            val offlinePlayers = Bukkit.getOfflinePlayers()
            for (job in Jobs.values())
                posCache.putAll(offlinePlayers.sortedByDescending { it.getJobLevel(job) }
                    .map { Pair(job, it.uniqueId) to it.getJobLevel(job) })
        }
        return posCache[Pair(job, uuid)]?.plus(1)
    }
}