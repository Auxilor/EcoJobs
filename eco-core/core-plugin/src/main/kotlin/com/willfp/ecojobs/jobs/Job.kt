package com.willfp.ecojobs.jobs

import com.github.benmanes.caffeine.cache.Caffeine
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.data.profile
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.placeholder.PlayerPlaceholder
import com.willfp.eco.core.placeholder.PlayerStaticPlaceholder
import com.willfp.eco.core.placeholder.PlayerlessPlaceholder
import com.willfp.eco.core.price.ConfiguredPrice
import com.willfp.eco.core.price.impl.PriceEconomy
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.toNiceString
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.canJoinJob
import com.willfp.ecojobs.api.event.PlayerJobExpGainEvent
import com.willfp.ecojobs.api.event.PlayerJobJoinEvent
import com.willfp.ecojobs.api.event.PlayerJobLeaveEvent
import com.willfp.ecojobs.api.event.PlayerJobLevelUpEvent
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.api.getJobProgress
import com.willfp.ecojobs.api.getJobXP
import com.willfp.ecojobs.api.getJobXPRequired
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.jobLimit
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.conditions.ConfiguredCondition
import com.willfp.libreforge.effects.ConfiguredEffect
import com.willfp.libreforge.effects.Effects
import com.willfp.libreforge.events.TriggerPreProcessEvent
import com.willfp.libreforge.triggers.Counters
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.DoubleSummaryStatistics
import java.util.Objects
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

class Job(
    val id: String, val config: Config, private val plugin: EcoJobsPlugin
) {
    val name = config.getFormattedString("name")
    val description = config.getFormattedString("description")
    val isUnlockedByDefault = config.getBool("unlocked-by-default")
    val resetsOnQuit = config.getBool("reset-on-quit")

    val joinPrice = ConfiguredPrice.create(config.getSubsection("join-price")) ?: ConfiguredPrice(
        PriceEconomy(config.getDouble("join-price")), ""
    )

    val leavePrice = ConfiguredPrice.create(config.getSubsection("leave-price")) ?: ConfiguredPrice(
        PriceEconomy(config.getDouble("leave-price")), ""
    )

    val levelKey: PersistentDataKey<Int> = PersistentDataKey(
        EcoJobsPlugin.instance.namespacedKeyFactory.create("${id}_level"),
        PersistentDataKeyType.INT,
        if (isUnlockedByDefault) 1 else 0
    )

    val xpKey: PersistentDataKey<Double> = PersistentDataKey(
        EcoJobsPlugin.instance.namespacedKeyFactory.create("${id}_xp"), PersistentDataKeyType.DOUBLE, 0.0
    )

    private val levelXpRequirements = listOf(0) + config.getInts("level-xp-requirements")

    val maxLevel = levelXpRequirements.size

    val levelGUI = JobLevelGUI(plugin, this)

    val leaveGUI = JobLeaveGUI(plugin, this)

    private val baseItem: ItemStack = Items.lookup(config.getString("icon")).item

    private val effects: Set<ConfiguredEffect>
    private val conditions: Set<ConfiguredCondition>

    private val levels = Caffeine.newBuilder().build<Int, JobLevel>()
    private val effectsDescription = Caffeine.newBuilder().build<Int, List<String>>()
    private val rewardsDescription = Caffeine.newBuilder().build<Int, List<String>>()
    private val levelUpMessages = Caffeine.newBuilder().build<Int, List<String>>()

    private val levelCommands = mutableMapOf<Int, MutableList<String>>()

    private val levelPlaceholders = config.getSubsections("level-placeholders").map { sub ->
        LevelPlaceholder(
            sub.getString("id")
        ) {
            NumberUtils.evaluateExpression(
                sub.getString("value").replace("%level%", it.toString())
            ).toNiceString()
        }
    }

    private val jobXpGains = config.getSubsections("xp-gain-methods").mapNotNull {
        Counters.compile(it, "Job $id")
    }

    init {
        config.injectPlaceholders(PlayerStaticPlaceholder(
            "level"
        ) { p ->
            p.getJobLevel(this).toString()
        })

        effects = config.getSubsections("effects").mapNotNull {
            Effects.compile(it, "Job $id")
        }.toSet()

        conditions = config.getSubsections("conditions").mapNotNull {
            Conditions.compile(it, "Job $id")
        }.toSet()

        for (string in config.getStrings("level-commands")) {
            val split = string.split(":")

            if (split.size == 1) {
                for (level in 1..maxLevel) {
                    val commands = levelCommands[level] ?: mutableListOf()
                    commands.add(string)
                    levelCommands[level] = commands
                }
            } else {
                val level = split[0].toInt()

                val command = string.removePrefix("$level:")
                val commands = levelCommands[level] ?: mutableListOf()
                commands.add(command)
                levelCommands[level] = commands
            }
        }

        PlayerPlaceholder(
            plugin, "${id}_percentage_progress"
        ) {
            (it.getJobProgress(this) * 100).toNiceString()
        }.register()

        PlayerPlaceholder(
            plugin, id
        ) {
            it.getJobLevel(this).toString()
        }.register()

        PlayerPlaceholder(
            plugin, "${id}_current_xp"
        ) {
            NumberUtils.format(it.getJobXP(this))
        }.register()

        PlayerPlaceholder(
            plugin, "${id}_required_xp"
        ) {
            it.getJobXPRequired(this).toString()
        }.register()

        PlayerlessPlaceholder(
            plugin, "${id}_name"
        ) {
            this.name
        }.register()

        PlayerPlaceholder(
            plugin, "${id}_level"
        ) {
            it.getJobLevel(this).toString()
        }.register()

        PlayerPlaceholder(
            plugin, "${id}_active"
        ) {
            it.hasJobActive(this).toString()
        }.register()

        PlayerPlaceholder(
            plugin, "${id}_total_players"
        ) {
            Bukkit.getOfflinePlayers().count { this in it.activeJobs }.toString()
        }.register()
    }

    fun getLevel(level: Int): JobLevel = levels.get(level) {
        JobLevel(this, it, effects, conditions)
    }

    private fun getLevelUpMessages(level: Int, whitespace: Int = 0): List<String> = levelUpMessages.get(level) {
        var highestConfiguredLevel = 1
        for (messagesLevel in this.config.getSubsection("level-up-messages").getKeys(false).map { it.toInt() }) {
            if (messagesLevel > level) {
                continue
            }

            if (messagesLevel > highestConfiguredLevel) {
                highestConfiguredLevel = messagesLevel
            }
        }

        this.config.getStrings("level-up-messages.$highestConfiguredLevel").map {
            levelPlaceholders.format(it, level)
        }.map {
            " ".repeat(whitespace) + it
        }
    }

    private fun getEffectsDescription(level: Int, whitespace: Int = 0): List<String> = effectsDescription.get(level) {
        var highestConfiguredLevel = 1
        for (messagesLevel in this.config.getSubsection("effects-description").getKeys(false).map { it.toInt() }) {
            if (messagesLevel > level) {
                continue
            }

            if (messagesLevel > highestConfiguredLevel) {
                highestConfiguredLevel = messagesLevel
            }
        }

        this.config.getStrings("effects-description.$highestConfiguredLevel").map {
            levelPlaceholders.format(it, level)
        }.map {
            " ".repeat(whitespace) + it
        }
    }

    private fun getRewardsDescription(level: Int, whitespace: Int = 0): List<String> = rewardsDescription.get(level) {
        var highestConfiguredLevel = 1
        for (messagesLevel in this.config.getSubsection("rewards-description").getKeys(false).map { it.toInt() }) {
            if (messagesLevel > level) {
                continue
            }

            if (messagesLevel > highestConfiguredLevel) {
                highestConfiguredLevel = messagesLevel
            }
        }

        this.config.getStrings("rewards-description.$highestConfiguredLevel").map {
            levelPlaceholders.format(it, level)
        }.map {
            " ".repeat(whitespace) + it
        }
    }

    private fun getLeaveLore(level: Int, whitespace: Int = 0): List<String> = this.config.getStrings("leave-lore").map {
        levelPlaceholders.format(it, level)
    }.map {
        " ".repeat(whitespace) + it
    }

    private fun getJoinLore(level: Int, whitespace: Int = 0): List<String> = this.config.getStrings("join-lore").map {
        levelPlaceholders.format(it, level)
    }.map {
        " ".repeat(whitespace) + it
    }

    fun injectPlaceholdersInto(lore: List<String>, player: Player, forceLevel: Int? = null): List<String> {
        val withPlaceholders = lore.map {
            it.replace("%percentage_progress%", (player.getJobProgress(this) * 100).toNiceString())
                .replace("%current_xp%", player.getJobXP(this).toNiceString())
                .replace("%required_xp%", this.getExpForLevel(player.getJobLevel(this) + 1).let { req ->
                    if (req == Int.MAX_VALUE) {
                        plugin.langYml.getFormattedString("infinity")
                    } else {
                        req.toNiceString()
                    }
                }).replace("%description%", this.description).replace("%job%", this.name)
                .replace("%level%", (forceLevel ?: player.getJobLevel(this)).toString())
                .replace("%level_numeral%", NumberUtils.toNumeral(forceLevel ?: player.getJobLevel(this)))
                .replace("%join_price%", this.joinPrice.getDisplay(player))
                .replace("%leave_price%", this.leavePrice.getDisplay(player))
        }.toMutableList()

        val processed = mutableListOf<List<String>>()

        for (s in withPlaceholders) {
            val whitespace = s.length - s.replace(" ", "").length

            processed.add(
                if (s.contains("%effects%")) {
                    getEffectsDescription(forceLevel ?: player.getJobLevel(this), whitespace)
                } else if (s.contains("%rewards%")) {
                    getRewardsDescription(forceLevel ?: player.getJobLevel(this), whitespace)
                } else if (s.contains("%level_up_messages%")) {
                    getLevelUpMessages(forceLevel ?: player.getJobLevel(this), whitespace)
                } else if (s.contains("%leave_lore%")) {
                    getLeaveLore(forceLevel ?: player.getJobLevel(this), whitespace)
                } else if (s.contains("%join_lore%")) {
                    getJoinLore(forceLevel ?: player.getJobLevel(this), whitespace)
                } else {
                    listOf(s)
                }
            )
        }

        return processed.flatten().formatEco(player)
    }

    fun getIcon(player: Player): ItemStack {
        val base = baseItem.clone()

        val level = player.getJobLevel(this)

        return ItemStackBuilder(base).setDisplayName(
            plugin.configYml.getFormattedString("gui.job-icon.name").replace("%level%", level.toString())
                .replace("%level_numeral%", NumberUtils.toNumeral(level)).replace("%job%", this.name)
        ).addLoreLines {
            injectPlaceholdersInto(
                plugin.configYml.getStrings("gui.job-icon.lore"), player
            ) + if (player.hasJobActive(this)) {
                plugin.configYml.getStrings("gui.job-icon.active-lore")
            } else if (player.canJoinJob(this)) {
                plugin.configYml.getStrings("gui.job-icon.join-lore")
            } else if (player.activeJobs.size == player.jobLimit) {
                plugin.configYml.getStrings("gui.job-icon.too-many-jobs-lore")
            } else {
                emptyList()
            }
        }.build()
    }

    fun getJobInfoIcon(player: Player): ItemStack {
        val base = baseItem.clone()
        return ItemStackBuilder(base).setDisplayName(
            plugin.configYml.getFormattedString("gui.job-info.active.name")
                .replace("%level%", player.getJobLevel(this).toString()).replace("%level_numeral%", NumberUtils.toNumeral(player.getJobLevel(this))).replace("%job%", this.name)
        ).addLoreLines {
            injectPlaceholdersInto(plugin.configYml.getStrings("gui.job-info.active.lore"), player)
        }.build()
    }

    fun getExpForLevel(level: Int): Int {
        if (level < 1 || level > maxLevel) {
            return Int.MAX_VALUE
        }

        return levelXpRequirements[level - 1]
    }

    fun executeLevelCommands(player: Player, level: Int) {
        val commands = levelCommands[level] ?: emptyList()

        for (command in commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.name))
        }
    }

    fun getXP(event: TriggerPreProcessEvent): Double {
        return jobXpGains.sumOf { it.getCount(event) }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Job) {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(this.id)
    }
}

private class LevelPlaceholder(
    val id: String, private val function: (Int) -> String
) {
    operator fun invoke(level: Int) = function(level)
}

private fun Collection<LevelPlaceholder>.format(string: String, level: Int): String {
    var process = string
    for (placeholder in this) {
        process = process.replace("%${placeholder.id}%", placeholder(level))
    }
    return process
}

fun OfflinePlayer.getJobLevelObject(job: Job): JobLevel = job.getLevel(this.getJobLevel(job))

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
