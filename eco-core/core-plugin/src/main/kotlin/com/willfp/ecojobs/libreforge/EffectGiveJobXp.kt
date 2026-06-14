package com.willfp.ecojobs.libreforge

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter

object EffectGiveJobXp : Effect<NoCompileData>("give_job_xp") {
    override val description = "Gives the player experience in the specified job."

    override val categories = setOf("economy", "player")

    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require(
            "amount",
            "You must specify the amount of xp to give!",
            description = "The amount of job experience to give to the player. Supports expressions.",
            type = ArgType.EXPRESSION
        )
        require(
            "job",
            "You must specify the job to give xp for!",
            description = "The id of the job to give experience for.",
            type = ArgType.STRING
        )
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player ?: return false

        player.giveJobExperience(
            Jobs.getByID(config.getString("job")) ?: return false,
            config.getDoubleFromExpression("amount", player)
        )

        return true
    }
}
