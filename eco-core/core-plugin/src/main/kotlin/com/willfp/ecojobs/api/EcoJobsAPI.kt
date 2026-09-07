@file:JvmName("EcoJobsAPI")

package com.willfp.ecojobs.api

import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.progression.LevelProgression
import com.willfp.eco.core.progression.StopReason
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
    val level = this.getJobLevel(job)

    return LevelProgression.progressFraction(
        this.getJobXP(job),
        job.curve.xpToReach(level + 1),
        level >= job.maxLevel
    )
}

/**
 * Reject an XP amount that cannot be granted.
 *
 * Deliberately not `abs()`: wrapping a negative amount in abs() turned a misconfigured effect
 * expression into a silent *gain*, which is the worst of both worlds - the mistake is hidden
 * and its effect is inverted.
 */
private fun validateXpAmount(amount: Double, context: String): Boolean {
    if (!amount.isFinite() || amount <= 0.0) {
        plugin.logger.warning("Refused a non-positive xp grant of $amount for $context")
        return false
    }

    return true
}

/**
 * Give job experience.
 *
 * XP only applies to a job the player has actually joined; see the config comment above the
 * `jobs` section for the decision this implements.
 */
@JvmOverloads
fun Player.giveJobExperience(job: Job, experience: Double, withMultipliers: Boolean = true) {
    if (!this.hasJobActive(job)) {
        return
    }

    val exp = if (withMultipliers) experience * this.jobExperienceMultiplier else experience

    if (!validateXpAmount(exp, "job ${job.id}")) {
        return
    }

    val gainEvent = PlayerJobExpGainEvent(this, job, exp, !withMultipliers)
    Bukkit.getPluginManager().callEvent(gainEvent)

    if (gainEvent.isCancelled) {
        return
    }

    this.giveExactJobExperience(job, gainEvent.amount)
}

/**
 * Give exact job experience, without calling PlayerJobExpGainEvent.
 *
 * Guarded the same as [giveJobExperience]: this function's documented purpose is to skip the
 * gain event, and it is reachable directly from commands and the API.
 */
fun Player.giveExactJobExperience(job: Job, experience: Double) {
    if (!this.hasJobActive(job)) {
        return
    }

    if (!validateXpAmount(experience, "job ${job.id}")) {
        return
    }

    val startLevel = this.getJobLevel(job)
    val change = LevelProgression.progress(job.curve, startLevel, this.getJobXP(job), experience)

    if (change.stopReason == StopReason.INVALID_REQUIREMENT) {
        job.warnBrokenCurveOnce(startLevel + 1)
    }

    this.setJobXP(job, change.newXp)

    val gained = change.levelsGained ?: return

    this.setJobLevel(job, change.newLevel)

    // One event per level crossed: a single grant spanning five levels must not swallow four
    // of the level-up rewards.
    for (level in gained) {
        Bukkit.getPluginManager().callEvent(PlayerJobLevelUpEvent(this, job, level))
    }
}
