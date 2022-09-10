package com.willfp.ecojobs

import com.willfp.ecojobs.api.EcoJobsAPI
import com.willfp.ecojobs.jobs.Job
import com.willfp.ecojobs.jobs.activeJob
import com.willfp.ecojobs.jobs.getJobLevel
import com.willfp.ecojobs.jobs.getJobProgress
import com.willfp.ecojobs.jobs.getJobXP
import com.willfp.ecojobs.jobs.getJobXPRequired
import com.willfp.ecojobs.jobs.giveJobExperience
import com.willfp.ecojobs.jobs.hasJob
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

internal object EcoJobsAPIImpl : EcoJobsAPI {
    override fun hasJob(player: OfflinePlayer, job: Job) = player.hasJob(job)

    override fun getActiveJob(player: OfflinePlayer): Job? = player.activeJob

    override fun getJobLevel(player: OfflinePlayer, job: Job) = player.getJobLevel(job)

    override fun giveJobExperience(player: Player, job: Job, amount: Double) =
        player.giveJobExperience(job, amount)

    override fun giveJobExperience(player: Player, job: Job, amount: Double, applyMultipliers: Boolean) =
        player.giveJobExperience(job, amount, noMultiply = !applyMultipliers)

    override fun getJobProgress(player: OfflinePlayer, job: Job) =
        player.getJobProgress(job)

    override fun getJobXPRequired(player: OfflinePlayer, job: Job) =
        player.getJobXPRequired(job)

    override fun getJobXP(player: OfflinePlayer, job: Job) =
        player.getJobXP(job)
}
