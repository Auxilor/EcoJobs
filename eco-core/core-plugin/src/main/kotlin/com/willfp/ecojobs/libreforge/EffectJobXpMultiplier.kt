package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.event.PlayerJobExpGainEvent
import com.willfp.ecojobs.jobs.Job
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.templates.MultiMultiplierEffect
import com.willfp.libreforge.toDispatcher
import org.bukkit.event.EventHandler

object EffectJobXpMultiplier : MultiMultiplierEffect<Job>("job_xp_multiplier") {
    override val description = "Multiplies XP earned for one or all EcoJobs jobs while the holder is active."

    override val categories = setOf("economy")

    override val arguments = arguments {
        require(
            "multiplier",
            "You must specify the multiplier!",
            description = "The XP multiplier. Supports expressions.",
            type = ArgType.EXPRESSION
        )
        optional(
            "jobs",
            description = "List of job names to apply the multiplier to. If omitted, applies to all jobs.",
            type = ArgType.STRING_LIST
        )
    }

    override val key = "jobs"

    override fun getElement(key: String): Job? {
        return Jobs.getByID(key)
    }

    override fun getAllElements(): Collection<Job> {
        return Jobs.values()
    }

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerJobExpGainEvent) {
        event.amount *= getMultiplier(event.player.toDispatcher(), event.job)
    }
}
