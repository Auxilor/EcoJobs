package com.willfp.ecojobs.api.event

import com.willfp.ecojobs.jobs.Job
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

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
