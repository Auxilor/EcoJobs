package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.entity.Player

class JobXPAccumulator(
    private val job: Job
) : Accumulator {
    override fun accept(player: Player, count: Double) {
        player.giveJobExperience(job, count)
    }
}
