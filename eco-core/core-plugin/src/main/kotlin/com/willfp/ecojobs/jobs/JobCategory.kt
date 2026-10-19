package com.willfp.ecojobs.jobs

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.registry.Registrable
import com.willfp.ecojobs.commands.CommandJobCategory
import org.bukkit.entity.Player
import java.util.Objects

class JobCategory(
    val id: String,
    val config: Config
) : Registrable {
    val name = config.getFormattedString("name")

    val limit = config.getInt("limit")

    val commandName = config.getStringOrNull("command")

    private val menu = JobsGUI.buildMenu(this)

    private val command = commandName?.let { CommandJobCategory(this, it) }

    init {
        command?.register()
    }

    fun open(player: Player) {
        menu.open(player)
    }

    fun remove() {
        command?.unregister()
    }

    override fun getID(): String {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is JobCategory) {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(this.id)
    }
}
