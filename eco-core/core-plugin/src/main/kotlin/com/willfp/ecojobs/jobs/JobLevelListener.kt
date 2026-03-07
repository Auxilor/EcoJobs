package com.willfp.ecojobs.jobs

import com.willfp.eco.core.sound.PlayableSound
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import com.willfp.libreforge.toDispatcher
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

        PlayableSound.create(plugin.configYml.getSubsection("level-up.sound"))?.playTo(player)

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
