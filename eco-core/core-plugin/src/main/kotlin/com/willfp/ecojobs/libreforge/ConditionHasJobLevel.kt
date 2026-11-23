package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.jobs.Jobs
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.condition.Condition
import wiki.ecohub.rules.holder.Dispatcher
import wiki.ecohub.rules.holder.ProvidedHolder
import wiki.ecohub.rules.holder.get
import wiki.ecohub.rules.holder.updateEffects

object ConditionHasJobLevel : Condition<NoCompileData>("has_job_level") {
    override val description = "Passes when the player's level in the specified job is at or above the given level."

    override val categories = setOf("player")

    override val arguments = arguments {
        require(
            "job",
            "You must specify the job!",
            description = "The id of the job to check the player's level in.",
            type = ArgType.STRING
        )
        require(
            "level",
            "You must specify the level!",
            description = "The minimum level required in the job.",
            type = ArgType.EXPRESSION
        )
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        return player.getJobLevel(
            Jobs.getByID(config.getString("job").lowercase()) ?: return false
        ) >= config.getIntFromExpression("level", player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun handle(event: PlayerJobLevelUpEvent) {
        event.player.toDispatcher().updateEffects()
    }
}
