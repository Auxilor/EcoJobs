package com.willfp.ecojobs.api.event

import com.willfp.ecojobs.jobs.Job
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerJobJoinEvent(
    val player: OfflinePlayer,
    val job: Job,
    val oldJob: Job?
) : Event(), Cancellable {
    private var cancelled = false

    override fun isCancelled() = this.cancelled

    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
