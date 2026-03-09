package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.plugin
import com.willfp.libreforge.Holder
import com.willfp.libreforge.conditions.ConditionList
import com.willfp.libreforge.effects.EffectList

class JobLevel(
    val job: Job,
    val level: Int,
    override val effects: EffectList,
    override val conditions: ConditionList
) : Holder {
    override val id = plugin.createNamespacedKey("${job.id}_$level")
}
