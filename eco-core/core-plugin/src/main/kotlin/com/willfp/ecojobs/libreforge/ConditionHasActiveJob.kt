package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.activeJobs
import org.bukkit.entity.Player
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.condition.Condition
import wiki.ecohub.rules.holder.Dispatcher
import wiki.ecohub.rules.holder.ProvidedHolder
import wiki.ecohub.rules.holder.get

object ConditionHasActiveJob : Condition<NoCompileData>("has_active_job") {
    override val description = "Passes when the player has joined the specified job."

    override val categories = setOf("player")

    override val arguments = arguments {
        require(
            "job",
            "You must specify the job!",
            description = "The id of the job the player must have joined.",
            type = ArgType.STRING
        )
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        return player.activeJobs.any { it.id == config.getString("job").lowercase() }
    }
}
