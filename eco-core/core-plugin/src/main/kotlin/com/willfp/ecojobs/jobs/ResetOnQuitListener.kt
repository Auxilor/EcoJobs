package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ResetOnQuitListener: Listener {
    @EventHandler
    fun onQuit(event: PlayerJobLeaveEvent) {
        val player = event.player
        val job = event.job

        if (job.resetsOnQuit) {
            player.resetJob(job)
        }
    }
}
