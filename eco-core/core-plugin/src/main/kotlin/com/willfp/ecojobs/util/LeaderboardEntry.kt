package com.willfp.ecojobs.util

import org.bukkit.OfflinePlayer

data class LeaderboardEntry(
    val player: OfflinePlayer,
    val level: Int
)