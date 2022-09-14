package com.willfp.ecojobs.jobs

import com.willfp.eco.core.integrations.economy.balance
import com.willfp.ecojobs.api.event.PlayerJobJoinEvent
import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object PriceHandler : Listener {
    @EventHandler(
        priority = EventPriority.LOW,
        ignoreCancelled = true
    )
    fun onJoin(event: PlayerJobJoinEvent) {
        val player = event.player
        val job = event.job
        val price = job.joinPrice

        if (price > 0) {
            val hasMoney = player.balance >= price

            if (!hasMoney) {
                event.isCancelled = true
            }

            player.balance -= price
        }
    }

    @EventHandler(
        priority = EventPriority.LOW,
        ignoreCancelled = true
    )
    fun onLeave(event: PlayerJobLeaveEvent) {
        val player = event.player
        val job = event.job
        val price = job.leavePrice

        if (price > 0) {
            val hasMoney = player.balance >= price

            if (!hasMoney) {
                event.isCancelled = true
            }

            player.balance -= price
        }
    }
}
