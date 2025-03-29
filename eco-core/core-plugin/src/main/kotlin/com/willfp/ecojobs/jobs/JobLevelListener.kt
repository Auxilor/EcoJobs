package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import com.willfp.libreforge.toDispatcher
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class JobLevelListener(
    private val plugin: EcoJobsPlugin
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onLevelUp(event: PlayerJobLevelUpEvent) {
        val job = event.job
        val player = event.player
        val level = event.level

        job.levelUpEffects?.trigger(player.toDispatcher())
        job.executeLevelCommands(player, level)

        if (this.plugin.configYml.getBool("level-up.sound.enabled")) {
            val sound = Sound.valueOf(this.plugin.configYml.getString("level-up.sound.id").uppercase())
            val pitch = this.plugin.configYml.getDouble("level-up.sound.pitch")

            player.playSound(
                player.location,
                sound,
                100f,
                pitch.toFloat()
            )
        }

        if (this.plugin.configYml.getBool("level-up.message.enabled")) {
            for (message in job.injectPlaceholdersInto(
                this.plugin.configYml.getFormattedStrings("level-up.message.message"),
                player,
                forceLevel = level
            )) {
                player.sendMessage(message)
            }
        }
    }
}
