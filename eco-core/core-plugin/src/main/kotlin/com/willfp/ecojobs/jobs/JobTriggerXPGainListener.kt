package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.libreforge.events.TriggerPreProcessEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object JobTriggerXPGainListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun handle(event: TriggerPreProcessEvent) {
        val player = event.player

        val jobs = event.player.activeJobs

        for (job in jobs) {
            val amount = job.getXP(event)

            if (amount <= 0.0) {
                return
            }

            player.giveJobExperience(job, amount)
        }
    }
}
