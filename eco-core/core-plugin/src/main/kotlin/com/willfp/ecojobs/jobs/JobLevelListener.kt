package com.willfp.ecojobs.jobs

import com.willfp.eco.util.SoundUtils
import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import com.willfp.ecojobs.plugin
import com.willfp.libreforge.toDispatcher
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object JobLevelListener : Listener {
    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR)
    fun onLevelUp(event: PlayerJobLevelUpEvent) {
        val job = event.job
        val player = event.player
        val level = event.level

        job.levelUpEffects?.trigger(player.toDispatcher())
        job.executeLevelCommands(player, level)

        if (plugin.configYml.getBool("level-up.sound.enabled")) {
            val sound = SoundUtils.getSound(plugin.configYml.getString("level-up.sound.id"))
            val pitch = plugin.configYml.getDouble("level-up.sound.pitch")

            if (sound != null) {
                player.playSound(
                    player.location,
                    sound,
                    100f,
                    pitch.toFloat()
                )
            }
        }

        if (plugin.configYml.getBool("level-up.message.enabled")) {
            for (message in job.injectPlaceholdersInto(
                plugin.configYml.getFormattedStrings("level-up.message.message"),
                player,
                forceLevel = level
            )) {
                player.sendMessage(message)
            }
        }
    }
}
