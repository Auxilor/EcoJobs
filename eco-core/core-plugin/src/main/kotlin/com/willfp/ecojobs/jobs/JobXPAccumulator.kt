package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.entity.Player

class JobXPAccumulator(
    private val job: Job
) : Accumulator {
    override fun accept(player: Player, count: Double) {
        if (!player.hasJobActive(job)) {
            return
        }

        player.giveJobExperience(job, count)
    }
}
