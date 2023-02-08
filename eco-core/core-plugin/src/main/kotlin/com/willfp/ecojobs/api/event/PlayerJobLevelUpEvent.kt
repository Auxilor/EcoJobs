package com.willfp.ecojobs.api.event

import org.bukkit.entity.Player
import com.willfp.ecojobs.jobs.Job
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.HandlerList

class PlayerJobLevelUpEvent(
    who: Player,
    override val job: Job,
    val level: Int
) : PlayerEvent(who), JobEvent {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
