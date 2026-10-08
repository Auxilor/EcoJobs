package com.willfp.ecojobs

import com.willfp.eco.core.Eco
import com.willfp.eco.core.Prerequisite
import org.bukkit.Bukkit
import org.bukkit.entity.Entity

/**
 * Run [block] on the region owning this entity: now if the current thread already owns it,
 * which is always the case off Folia, otherwise on the entity's next tick.
 */
internal inline fun Entity.runOwned(crossinline block: () -> Unit) {
    if (Eco.get().isOwnedByCurrentRegion(this)) {
        block()
    } else {
        plugin.scheduler.on(this).run { block() }
    }
}

/**
 * Run [block] on the global region: now if the current thread is already the global region,
 * which is always the case off Folia, otherwise on the next global tick.
 */
internal inline fun runGlobal(crossinline block: () -> Unit) {
    if (Prerequisite.HAS_FOLIA.isMet && !Bukkit.isGlobalTickThread()) {
        plugin.scheduler.global().run { block() }
    } else {
        block()
    }
}
