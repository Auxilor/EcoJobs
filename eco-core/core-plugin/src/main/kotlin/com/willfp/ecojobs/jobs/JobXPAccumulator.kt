package com.willfp.ecojobs.jobs

import com.willfp.eco.core.cache.EcoCache
import com.willfp.eco.core.integrations.afk.AFKManager
import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.plugin
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.GameMode
import org.bukkit.entity.Player
import kotlin.math.max
import java.time.Duration

class JobXPAccumulator(
    private val job: Job
) : Accumulator {
    override fun accept(player: Player, count: Double) {
        if (!player.hasJobActive(job)) {
            return
        }

        if (plugin.configYml.getBool("jobs.prevent-levelling-while-afk") && AFKManager.isAfk(player)) {
            return
        }

        if (player.gameMode in setOf(GameMode.CREATIVE, GameMode.SPECTATOR)) {
            return
        }

        player.giveJobExperience(job, count)
    }
}

private val expMultiplierCache = EcoCache.builder<Player, Double>().expireAfterWrite(Duration.ofSeconds(10)).build {
    it.cacheJobExperienceMultiplier()
}

val Player.jobExperienceMultiplier: Double
    get() = expMultiplierCache.get(this)

private fun Player.cacheJobExperienceMultiplier(): Double {
    if (this.hasPermission("ecojobs.xpmultiplier.quadruple")) {
        return 4.0
    }

    if (this.hasPermission("ecojobs.xpmultiplier.triple")) {
        return 3.0
    }

    if (this.hasPermission("ecojobs.xpmultiplier.double")) {
        return 2.0
    }

    if (this.hasPermission("ecojobs.xpmultiplier.50percent")) {
        return 1.5
    }

    return 1 + getNumericalPermission("ecojobs.xpmultiplier", 0.0) / 100
}

fun Player.getNumericalPermission(permission: String, default: Double): Double {
    var highest: Double? = null

    for (permissionAttachmentInfo in this.effectivePermissions) {
        val perm = permissionAttachmentInfo.permission
        if (perm.startsWith(permission)) {
            val found = perm.substring(perm.lastIndexOf(".") + 1).toDoubleOrNull() ?: continue
            highest = max(highest ?: Double.MIN_VALUE, found)
        }
    }

    return highest ?: default
}
