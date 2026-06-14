package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.triggers.Trigger
import com.willfp.libreforge.triggers.TriggerData
import com.willfp.libreforge.triggers.TriggerParameter
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

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
