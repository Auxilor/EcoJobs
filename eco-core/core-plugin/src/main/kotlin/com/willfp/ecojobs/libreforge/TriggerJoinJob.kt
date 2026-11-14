package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.event.PlayerJobJoinEvent
import com.willfp.ecojobs.api.getJobLevel
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import wiki.ecohub.game.entity.toDispatcher
import wiki.ecohub.rules.trigger.Trigger
import wiki.ecohub.rules.trigger.TriggerData
import wiki.ecohub.rules.trigger.TriggerParameter

object TriggerJoinJob : Trigger("join_job") {
    override val description = "Fires when the player joins a job."

    override val categories = setOf("player")

    override val parameters = setOf(
        TriggerParameter.PLAYER,
        TriggerParameter.LOCATION,
        TriggerParameter.EVENT,
        TriggerParameter.VALUE
    )

    override val parameterDescriptions = mapOf(
        TriggerParameter.VALUE to "The player's level in the job they joined"
    )

    @EventHandler(ignoreCancelled = true)
    fun handle(event: PlayerJobJoinEvent) {
        val player = event.player as? Player ?: return

        this.dispatch(
            player.toDispatcher(),
            TriggerData(
                player = player,
                location = player.location,
                event = event,
                value = player.getJobLevel(event.job).toDouble()
            )
        )
    }
}
