package com.willfp.ecojobs.jobs

import com.willfp.eco.core.integrations.economy.balance
import com.willfp.ecojobs.api.event.PlayerJobJoinEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object JoinPriceHandler : Listener {
    @EventHandler
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
}
