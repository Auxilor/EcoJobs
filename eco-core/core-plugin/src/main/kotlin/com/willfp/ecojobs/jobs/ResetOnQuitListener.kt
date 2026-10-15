package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import com.willfp.ecojobs.api.resetJob
import com.willfp.ecojobs.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ResetOnQuitListener : Listener {
    @EventHandler(
        ignoreCancelled = true
    )
    fun onQuit(event: PlayerJobLeaveEvent) {
        val player = event.player
        val job = event.job

        if (job.resetsOnQuit) {
            player.resetJob(job, plugin.configYml.getBool("jobs.reset-on-quit-relocks"))
        }
    }
}
