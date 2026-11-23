package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.plugin
import wiki.ecohub.rules.condition.ConditionList
import wiki.ecohub.rules.effect.EffectList
import wiki.ecohub.rules.holder.Holder

class JobLevel(
    val job: Job,
    val level: Int,
    override val effects: EffectList,
    override val conditions: ConditionList
) : Holder {
    override val id = plugin.createNamespacedKey("${job.id}_$level")
}
