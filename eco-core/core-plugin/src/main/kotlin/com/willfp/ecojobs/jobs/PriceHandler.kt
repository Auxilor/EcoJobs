package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.event.PlayerJobJoinEvent
import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object PriceHandler : Listener {
    @EventHandler(
        priority = EventPriority.LOW,
        ignoreCancelled = true
    )
    fun onJoin(event: PlayerJobJoinEvent) {
        val player = event.player as? Player ?: return
        val job = event.job
        val price = job.joinPrice

        if (!price.canAfford(player)) {
            event.isCancelled = true
            return
        }

        price.pay(player)
    }

    @EventHandler(
        priority = EventPriority.LOW,
        ignoreCancelled = true
    )
    fun onLeave(event: PlayerJobLeaveEvent) {
        val player = event.player as? Player ?: return
        val job = event.job
        val price = job.leavePrice

        if (!price.canAfford(player)) {
            event.isCancelled = true
            return
        }

        price.pay(player)
    }
}
