package com.willfp.ecojobs.jobs

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.libreforge.counters.Accumulator
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import kotlin.math.max

class JobXPAccumulator(
    private val job: Job
) : Accumulator {
    override fun accept(player: Player, count: Double) {
        if (!player.hasJobActive(job)) {
            return
        }

        player.giveJobExperience(job, count)
    }
}

private val expMultiplierCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build<Player, Double> {
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
