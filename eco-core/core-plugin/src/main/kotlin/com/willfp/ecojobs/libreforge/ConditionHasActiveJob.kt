package com.willfp.ecojobs.libreforge

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.ecojobs.api.activeJobs
import com.willfp.libreforge.Dispatcher
import com.willfp.libreforge.NoCompileData
import com.willfp.libreforge.ProvidedHolder
import com.willfp.libreforge.arguments
import com.willfp.libreforge.conditions.Condition
import com.willfp.libreforge.get
import org.bukkit.entity.Player

object ConditionHasActiveJob : Condition<NoCompileData>("has_active_job") {
    override val arguments = arguments {
        require("job", "You must specify the job!")
    }

    override fun isMet(
        dispatcher: Dispatcher<*>,
        config: Config,
        holder: ProvidedHolder,
        compileData: NoCompileData
    ): Boolean {
        val player = dispatcher.get<Player>() ?: return false

        return player.activeJobs.any { it.id == config.getString("job").lowercase() }
    }
}
