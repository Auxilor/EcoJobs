package com.willfp.ecojobs.api.event

import com.willfp.ecojobs.jobs.Job
import org.bukkit.OfflinePlayer
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerJobJoinEvent(
    val player: OfflinePlayer,
    override val job: Job,
    val oldJob: Job?
) : Event(), Cancellable, JobEvent {
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
