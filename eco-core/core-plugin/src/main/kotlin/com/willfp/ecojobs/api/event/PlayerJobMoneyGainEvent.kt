package com.willfp.ecojobs.api.event

import com.willfp.ecojobs.jobs.Job
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerJobMoneyGainEvent(
    who: Player,
    override val job: Job,
    var amount: Double
) : PlayerEvent(who), Cancellable, JobEvent {
    private var cancelled = false

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun isCancelled() = cancelled

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
