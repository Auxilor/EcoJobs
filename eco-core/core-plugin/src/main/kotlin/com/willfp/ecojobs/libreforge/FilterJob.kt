package com.willfp.ecojobs.libreforge

import com.willfp.ecojobs.api.event.JobEvent
import wiki.ecohub.config.ArgType
import wiki.ecohub.config.Config
import wiki.ecohub.core.registry.NoCompileData
import wiki.ecohub.rules.filter.Filter
import wiki.ecohub.rules.trigger.TriggerData

object FilterJob : Filter<NoCompileData, Collection<String>>("job") {
    override val description = "Matches when the job involved in the triggering event is one of the given job ids."

    override val categories = setOf("player")

    override val valueType = ArgType.STRING_LIST

    override val additionalInfo = listOf(
        "Passes automatically when the trigger event is not related to a job."
    )

    override fun getValue(config: Config, data: TriggerData?, key: String): Collection<String> {
        return config.getStrings(key)
    }

    override fun isMet(data: TriggerData, value: Collection<String>, compileData: NoCompileData): Boolean {
        val event = data.event as? JobEvent ?: return true

        return value.any { jobName ->
            jobName.equals(event.job.id, ignoreCase = true)
        }
    }
}
