package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.giveJobMoney
import com.willfp.ecojobs.jobs.Jobs
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.config.arguments
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.effect.Effect
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object EffectGiveJobMoney : Effect<NoCompileData>("give_job_money") {
    override val description = "Gives the player money for the specified job. " +
        "Does nothing if the player has not joined the job. Returns false when no money was " +
        "granted, so a configured cost or cooldown is not applied for a failed grant."

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

        return player.giveJobMoney(job, config.getDoubleFromExpression("amount", player))
    }
}
