package com.willfp.ecojobs.api.event

import org.bukkit.entity.Player
import com.willfp.ecojobs.jobs.Job
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.HandlerList
import org.bukkit.event.Cancellable

class PlayerJobExpGainEvent(
    who: Player,
    val job: Job,
    var amount: Double,
    val isMultiply: Boolean
) : PlayerEvent(who), Cancellable {
    private var cancelled = false

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
