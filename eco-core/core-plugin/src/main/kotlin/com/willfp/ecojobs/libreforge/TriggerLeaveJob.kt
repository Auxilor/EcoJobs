package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import com.willfp.ecojobs.api.getJobLevel
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.trigger.Trigger
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object TriggerLeaveJob : Trigger("leave_job") {
    override val description = "Fires when the player leaves a job."

    override val categories = setOf("player")

    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION,
        TriggerParameter.VALUE
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.VALUE to "The player's level in the job they left"
    )

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerJobLeaveEvent) {
        val player = event.player as? Player ?: return

        this.dispatch(
            player.toDispatcher(),
            TriggerData(
                player = player,
                location = player.location,
                value = player.getJobLevel(event.job).toDouble()
            )
        )
    }
}
