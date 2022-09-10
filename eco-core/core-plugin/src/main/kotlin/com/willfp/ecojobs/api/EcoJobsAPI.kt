package com.willfp.ecojobs.api

import com.willfp.ecojobs.EcoJobsAPIImpl
import com.willfp.ecojobs.jobs.Job
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

interface EcoJobsAPI {
    /**
     * Get if a player has a job.
     *
     * @param player The player.
     * @param job    The job.
     * @return If the player has the job unlocked.
     */
    fun hasJob(
        player: OfflinePlayer,
        job: Job
    ): Boolean

    /**
     * Get a player's active job.
     *
     * @param player The player.
     * @return The active job.
     */
    fun getActiveJob(
        player: OfflinePlayer
    ): Job?

    /**
     * Get a player's level of a certain job.
     *
     * @param player The player.
     * @param job    The job.
     * @return The level.
     */
    fun getJobLevel(
        player: OfflinePlayer,
        job: Job
    ): Int

    /**
     * Give job experience to a player.
     *
     * @param player The player.
     * @param job    The job.
     * @param amount The amount of experience to give.
     */
    fun giveJobExperience(
        player: Player,
        job: Job,
        amount: Double
    )

    /**
     * Give job experience to a player.
     *
     * @param player           The player.
     * @param job              The job.
     * @param amount           The amount of experience to give.
     * @param applyMultipliers If multipliers should be applied.
     */
    fun giveJobExperience(
        player: Player,
        job: Job,
        amount: Double,
        applyMultipliers: Boolean
    )

    /**
     * Get progress to next level between 0 and 1, where 0 is none and 1 is complete.
     *
     * @param player The player.
     * @param job    The job.
     * @return The progress.
     */
    fun getJobProgress(
        player: OfflinePlayer,
        job: Job
    ): Double

    /**
     * Get the experience required to advance to the next level.
     *
     * @param player The player.
     * @param job    The job.
     * @return The experience required.
     */
    fun getJobXPRequired(
        player: OfflinePlayer,
        job: Job
    ): Int

    /**
     * Get experience to the next level.
     *
     * @param player The player.
     * @param job    The job.
     * @return The experience.
     */
    fun getJobXP(
        player: OfflinePlayer,
        job: Job
    ): Double

    companion object {
        /**
         * Get the instance of the API.
         *
         * @return The API.
         */
        @JvmStatic
        val instance: EcoJobsAPI
            get() = EcoJobsAPIImpl
    }
}
