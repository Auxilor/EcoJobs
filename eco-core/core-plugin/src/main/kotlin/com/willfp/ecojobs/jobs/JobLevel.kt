package com.willfp.ecojobs.jobs

import com.willfp.libreforge.Holder
import com.willfp.libreforge.conditions.ConfiguredCondition
import com.willfp.libreforge.effects.ConfiguredEffect

class JobLevel(
    val job: Job,
    val level: Int,
    override val effects: Set<ConfiguredEffect>,
    override val conditions: Set<ConfiguredCondition>
): Holder {
    override val id = "${job.id}_$level"
}
