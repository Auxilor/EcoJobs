package com.willfp.ecojobs.libreforge

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import com.willfp.libreforge.toDispatcher
import com.willfp.libreforge.updateEffects
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

object ConditionHasJobLevel : Condition<NoCompileData>("has_job_level") {
    override val arguments = arguments {
        require("job", "You must specify the job!")
        require("level", "You must specify the level!")
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        return player.getJobLevel(
            Jobs.getByID(config.getString("job").lowercase()) ?: return false
        ) >= config.getIntFromExpression("level", player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun handle(event: PlayerJobLevelUpEvent) {
        event.player.toDispatcher().updateEffects()
    }
}
