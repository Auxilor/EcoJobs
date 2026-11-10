package com.willfp.ecojobs.libreforge

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.ecojobs.api.giveJobMoney
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.libreforge.ArgType
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.arguments
import com.willfp.libreforge.effects.Effect
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter

object EffectGiveJobMoney : Effect<NoCompileData>("give_job_money") {
    override val description = "Gives the player money for the specified job. " +
        "Does nothing if the player has not joined the job."

    override val categories = setOf("economy", "player")

    override val parameters = setOf(
        TriggerParameter.PLAYER
    )

    override val arguments = arguments {
        require(
            name = "amount",
            message = "You must specify the amount of money to give!",
            description = "The amount of money to give to the player. Supports expressions.",
            type = ArgType.EXPRESSION,
            example = "%level% * 0.4"
        )
        require(
            name = "job",
            message = "You must specify the job to give money for!",
            description = "The id of the job to give money for.",
            type = ArgType.STRING
        )
    }

    override fun onTrigger(config: Config, data: TriggerData, compileData: NoCompileData): Boolean {
        val player = data.player
        val job = player?.let { Jobs.getByID(config.getString("job")) }

        if (player == null || job == null) {
            return false
        }

        player.giveJobMoney(job, config.getDoubleFromExpression("amount", player))
        return true
    }
}
