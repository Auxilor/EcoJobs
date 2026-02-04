@file:JvmName("EcoJobsAPI")

package com.willfp.ecojobs.api

import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.event.PlayerJobExpGainEvent
import com.willfp.ecojobs.api.event.PlayerJobJoinEvent
import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import com.willfp.ecojobs.jobs.Job
import com.willfp.ecojobs.jobs.Jobs
import com.willfp.ecojobs.jobs.getNumericalPermission
import com.willfp.ecojobs.jobs.jobExperienceMultiplier
import com.willfp.ecojobs.plugin
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import kotlin.math.abs

/*

The old key is around for backwards compatibility with 1.x.x.

 */

private val legacyActiveJobKey: PersistentDataKey<String> = PersistentDataKey(
    plugin.namespacedKeyFactory.create("active_job"), PersistentDataKeyType.STRING, ""
)

private val activeJobsKey: PersistentDataKey<List<String>> = PersistentDataKey(
    plugin.namespacedKeyFactory.create("active_jobs"), PersistentDataKeyType.STRING_LIST, listOf()
)

/**
 * The job limit.
 */
val Player.jobLimit: Int
    get() {
        return this.getNumericalPermission("ecojobs.limit", plugin.configYml.getDouble("jobs.limit")).toInt()
    }

/**
 * If a player can join a job.
 */
fun Player.canJoinJob(job: Job): Boolean {
    return this.activeJobs.size < this.jobLimit && job !in this.activeJobs && this.hasJob(job)
}

/**
 * Get if a job is unlocked.
 */
fun OfflinePlayer.hasJob(job: Job) = this.getJobLevel(job) > 0

/**
 * Get if a player has a job active.
 */
fun OfflinePlayer.hasJobActive(job: Job) = job in this.activeJobs

/**
 * Get a player's active jobs.
 */
val OfflinePlayer.activeJobs: Collection<Job>
    get() {
        if (this.profile.read(legacyActiveJobKey).isNotBlank()) {
            this.profile.write(activeJobsKey, listOf(this.profile.read(legacyActiveJobKey)))
            this.profile.write(legacyActiveJobKey, "")
        }

        return this.profile.read(activeJobsKey).mapNotNull { Jobs.getByID(it) }

    }

/**
 * Join a job.
 */
fun OfflinePlayer.joinJob(job: Job) {
    val event = PlayerJobJoinEvent(this, job)
    Bukkit.getPluginManager().callEvent(event)

    if (!event.isCancelled) {
        this.profile.write(activeJobsKey, this.activeJobs.plus(job).map { it.id })
    }
}

/**
 * Leave a job.
 */
fun OfflinePlayer.leaveJob(job: Job) {
    if (job !in this.activeJobs) {
        return
    }

    val event = PlayerJobLeaveEvent(this, job)
    Bukkit.getPluginManager().callEvent(event)

    if (!event.isCancelled) {
        this.forceLeaveJob(job)
    }
}

/**
 * Leave a job without checking.
 */
fun OfflinePlayer.forceLeaveJob(job: Job) {
    this.profile.write(activeJobsKey, this.activeJobs.minus(job).map { it.id })
}

/**
 * Get the level of a certain job.
 */
fun OfflinePlayer.getJobLevel(job: Job) = this.profile.read(job.levelKey)

/**
 * Set the level of a certain job.
 */
fun OfflinePlayer.setJobLevel(job: Job, level: Int) = this.profile.write(job.levelKey, level)

/**
 * Get current job experience.
 */
fun OfflinePlayer.getJobXP(job: Job) = this.profile.read(job.xpKey)

/**
 * Set current job experience.
 */
fun OfflinePlayer.setJobXP(job: Job, xp: Double) = this.profile.write(job.xpKey, xp)

/**
 * Reset a job.
 */
fun OfflinePlayer.resetJob(job: Job) {
    this.setJobLevel(job, 1)
    this.setJobXP(job, 0.0)
}

/**
 * Get the experience required to advance to the next level.
 */
fun OfflinePlayer.getJobXPRequired(job: Job) = job.getFormattedExpForLevel(this.getJobLevel(job) + 1)

/**
 * Get progress to next level between 0 and 1, where 0 is none and 1 is complete.
 */
fun OfflinePlayer.getJobProgress(job: Job): Double {
    val currentXP = this.getJobXP(job)
    val requiredXP = job.getExpForLevel(this.getJobLevel(job) + 1)
    return currentXP / requiredXP
}

/**
 * Give job experience.
 */
@JvmOverloads
fun Player.giveJobExperience(job: Job, experience: Double, withMultipliers: Boolean = true) {
    val exp = abs(
        if (withMultipliers) experience * this.jobExperienceMultiplier
        else experience
    )

    val gainEvent = PlayerJobExpGainEvent(this, job, exp, !withMultipliers)
    Bukkit.getPluginManager().callEvent(gainEvent)

    if (gainEvent.isCancelled) {
        return
    }

    this.giveExactJobExperience(job, gainEvent.amount)
}

/**
 * Give exact job experience, without calling PlayerJobExpGainEvent.
 */
fun Player.giveExactJobExperience(job: Job, experience: Double) {
    val level = this.getJobLevel(job)

    val progress = this.getJobXP(job) + experience

    if (progress >= job.getExpForLevel(level + 1) && level + 1 <= job.maxLevel) {
        val overshoot = progress - job.getExpForLevel(level + 1)
        this.setJobXP(job, 0.0)
        this.setJobLevel(job, level + 1)
        val levelUpEvent = PlayerJobLevelUpEvent(this, job, level + 1)
        Bukkit.getPluginManager().callEvent(levelUpEvent)
        this.giveExactJobExperience(job, overshoot)
    } else {
        this.setJobXP(job, progress)
    }
}
