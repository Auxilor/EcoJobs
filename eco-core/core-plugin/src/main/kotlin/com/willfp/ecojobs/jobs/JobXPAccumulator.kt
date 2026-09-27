package com.willfp.ecojobs.jobs

import com.willfp.eco.core.cache.EcoCache
import com.willfp.eco.core.integrations.afk.AFKManager
import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.plugin
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.time.Duration
import com.willfp.eco.util.NumericalPermissions

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
    get() = expMultiplierCache.get(this) { it.cacheJobExperienceMultiplier() }

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
    // Delegates to eco so the four copies of this loop cannot drift apart again. Two
    // behaviour fixes come with it: a permission explicitly set to false no longer counts,
    // and a negative value is honoured rather than lost to a `Double.MIN_VALUE` seed - which
    // is the smallest *positive* double, so `.-50` used to resolve to roughly zero.
    //
    // The node itself stays this plugin's own; eco supplies the arithmetic, never a prefix.
    return NumericalPermissions.highest(
        this.effectivePermissions.filter { it.value }.map { it.permission },
        permission,
        default
    )
}
