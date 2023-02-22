package com.willfp.ecojobs

import com.willfp.eco.core.math.MathContext
import com.willfp.eco.core.price.Price
import com.willfp.eco.core.price.PriceFactory
import com.willfp.ecojobs.jobs.Job
import com.willfp.ecojobs.jobs.getJobLevel
import com.willfp.ecojobs.jobs.setJobLevel
import com.willfp.ecojobs.jobs.setJobXP
import org.bukkit.entity.Player
import java.util.UUID
import java.util.function.Function
import kotlin.math.roundToInt

class PriceFactoryJobLevel(private val job: Job) : PriceFactory {
    override fun getNames() = listOf(
        "ecojobs:${job.id}_level"
    )

    override fun create(baseContext: MathContext, function: Function<MathContext, Double>): Price {
        return JobLevel(baseContext, job) { function.apply(it).roundToInt() }
    }

    private class JobLevel(
        private val baseContext: MathContext,
        val job: Job,
        private val level: (MathContext) -> Int
    ) : Price {
        private val multipliers = mutableMapOf<UUID, Double>()

        override fun canAfford(player: Player, multiplier: Double) =
            player.getJobLevel(job) >= getValue(player, multiplier)

        override fun pay(player: Player, multiplier: Double) {
            val newLevel = player.getJobLevel(job) - getValue(player, multiplier).roundToInt()
            player.setJobLevel(job, if (newLevel >= 1) newLevel else 1)
            player.setJobXP(job, 0.0)
        }

        override fun giveTo(player: Player, multiplier: Double) {
            player.setJobLevel(job, player.getJobLevel(job) + getValue(player, multiplier).roundToInt())
        }

        override fun getValue(player: Player, multiplier: Double): Double {
            return level(MathContext.copyWithPlayer(baseContext, player)) * getMultiplier(player) * multiplier
        }

        override fun getMultiplier(player: Player): Double {
            return multipliers[player.uniqueId] ?: 1.0
        }

        override fun setMultiplier(player: Player, multiplier: Double) {
            multipliers[player.uniqueId] = multiplier.roundToInt().toDouble()
        }
    }
}
