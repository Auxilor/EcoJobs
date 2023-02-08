package com.willfp.ecojobs.api.event

import com.willfp.ecojobs.jobs.Job

interface JobEvent {
    val job: Job
}
