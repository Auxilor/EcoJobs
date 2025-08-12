package com.willfp.ecojobs.util

import org.bukkit.OfflinePlayer

data class LeaderboardCacheEntry(
    val player: OfflinePlayer,
    val level: Int
)