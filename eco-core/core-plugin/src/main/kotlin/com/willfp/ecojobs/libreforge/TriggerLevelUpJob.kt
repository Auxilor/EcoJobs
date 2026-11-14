package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import org.bukkit.event.EventHandler
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.trigger.Trigger
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object TriggerLevelUpJob : Trigger("level_up_job") {
    override val description = "Fires when the player levels up in a job."

    override val categories = setOf("player")

    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION,
        TriggerParameter.EVENT,
        TriggerParameter.VALUE
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.VALUE to "The new job level the player reached"
    )

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerJobLevelUpEvent) {
        val player = event.player

        this.dispatch(
            player.toDispatcher(),
            TriggerData(
                player = player,
                location = player.location,
                event = event,
                value = event.level.toDouble()
            )
        )
    }
}
