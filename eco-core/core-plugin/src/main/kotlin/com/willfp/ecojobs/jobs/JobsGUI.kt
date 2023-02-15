package com.willfp.ecojobs.jobs

import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.onRightClick
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.items.builder.SkullBuilder
import com.willfp.eco.util.formatEco
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.canJoinJob
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.joinJob
import com.willfp.ecojobs.jobs.Jobs.unlockedJobs
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

object JobsGUI {
    private lateinit var menu: Menu
    private val jobAreaSlots = mutableListOf<Pair<Int, Int>>()

    @JvmStatic
    @ConfigUpdater
    fun update(plugin: EcoJobsPlugin) {
        val topLeftRow = plugin.configYml.getInt("gui.job-area.top-left.row")
        val topLeftColumn = plugin.configYml.getInt("gui.job-area.top-left.column")
        val bottomRightRow = plugin.configYml.getInt("gui.job-area.bottom-right.row")
        val bottomRightColumn = plugin.configYml.getInt("gui.job-area.bottom-right.column")

        jobAreaSlots.clear()
        for (row in topLeftRow..bottomRightRow) {
            for (column in topLeftColumn..bottomRightColumn) {
                jobAreaSlots.add(Pair(row, column))
            }
        }

        menu = buildMenu(plugin)
    }

    private fun buildMenu(plugin: EcoJobsPlugin): Menu {
        val jobIconBuilder = { player: Player, menu: Menu, index: Int ->
            val page = menu.getPage(player)

            val unlockedJobs = player.unlockedJobs

            val pagedIndex = ((page - 1) * jobAreaSlots.size) + index

            val job = unlockedJobs.getOrNull(pagedIndex)
            job?.getIcon(player) ?: ItemStack(Material.AIR)
        }

        return menu(plugin.configYml.getInt("gui.rows")) {
            title = plugin.langYml.getString("menu.title")

            setMask(
                FillerMask(
                    MaskItems.fromItemNames(plugin.configYml.getStrings("gui.mask.materials")),
                    *plugin.configYml.getStrings("gui.mask.pattern").toTypedArray()
                )
            )

            setSlot(
                plugin.configYml.getInt("gui.player-info.row"),
                plugin.configYml.getInt("gui.player-info.column"),
                slot { player, _ ->
                    val skullBuilder = SkullBuilder()
                        .setDisplayName(
                            plugin.configYml.getString("gui.player-info.name")
                                .replace("%player%", player.displayName)
                                .formatEco(player, true)
                        )

                    if (player.activeJobs.isEmpty()) {
                        skullBuilder.addLoreLines(
                            plugin.configYml.getStrings("gui.player-info.no-jobs")
                                .formatEco(player, true)
                        )
                    } else {
                        skullBuilder.addLoreLines(
                            plugin.configYml.getStrings("gui.player-info.has-jobs")
                                .flatMap {
                                    if (it == "%jobs%") {
                                        player.activeJobs.flatMap { job ->
                                            job.injectPlaceholdersInto(
                                                plugin.configYml.getStrings("gui.player-info.job-line"),
                                                player
                                            )
                                        }
                                    } else {
                                        listOf(it)
                                    }
                                }
                                .formatEco(player, true)
                        )
                    }

                    val skull = skullBuilder.build()

                    val meta = skull.itemMeta as SkullMeta
                    meta.owningPlayer = player
                    skull.itemMeta = meta
                    skull
                }
            )

            for ((index, pair) in jobAreaSlots.withIndex()) {
                val (row, column) = pair

                setSlot(row, column, slot({ p, m -> jobIconBuilder(p, m, index) }) {
                    onLeftClick { player, _, _, menu ->
                        val page = menu.getPage(player)

                        val unlockedJobs = player.unlockedJobs

                        val pagedIndex = ((page - 1) * jobAreaSlots.size) + index

                        val job = unlockedJobs.getOrNull(pagedIndex) ?: return@onLeftClick

                        if (player.hasJobActive(job)) {
                            job.levelGUI.open(player)
                        } else {
                            if (player.canJoinJob(job)) {
                                player.joinJob(job)

                                if (player.hasJobActive(job)) {
                                    player.sendMessage(
                                        plugin.langYml.getMessage("joined-job")
                                            .replace("%job%", job.name)
                                    )
                                }
                            } else {
                                player.sendMessage(plugin.langYml.getMessage("cannot-join-job"))
                                return@onLeftClick
                            }
                        }

                        player.playSound(
                            player.location,
                            Sound.valueOf(plugin.configYml.getString("gui.job-icon.click.sound").uppercase()),
                            1f,
                            plugin.configYml.getDouble("gui.job-icon.click.pitch").toFloat()
                        )
                    }

                    onRightClick { player, _, _, menu ->
                        val page = menu.getPage(player)

                        val unlockedJobs = player.unlockedJobs

                        val pagedIndex = ((page - 1) * jobAreaSlots.size) + index

                        val job = unlockedJobs.getOrNull(pagedIndex) ?: return@onRightClick

                        if (player.hasJobActive(job)) {
                            job.leaveGUI.open(player)

                            player.playSound(
                                player.location,
                                Sound.valueOf(plugin.configYml.getString("gui.job-icon.click.sound").uppercase()),
                                1f,
                                plugin.configYml.getDouble("gui.job-icon.click.pitch").toFloat()
                            )
                        }
                    }
                })
            }

            addComponent(
                plugin.configYml.getInt("gui.prev-page.location.row"),
                plugin.configYml.getInt("gui.prev-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.prev-page.item")))
                        .setDisplayName(plugin.configYml.getString("gui.prev-page.name"))
                        .build(),
                    PageChanger.Direction.BACKWARDS
                )
            )

            addComponent(
                plugin.configYml.getInt("gui.next-page.location.row"),
                plugin.configYml.getInt("gui.next-page.location.column"),
                PageChanger(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.next-page.item")))
                        .setDisplayName(plugin.configYml.getString("gui.next-page.name"))
                        .build(),
                    PageChanger.Direction.FORWARDS
                )
            )

            maxPages { player ->
                ceil(Jobs.values()
                    .filter { player.getJobLevel(it) > 0 }
                    .size.toDouble() / jobAreaSlots.size).toInt()
            }

            setSlot(plugin.configYml.getInt("gui.close.location.row"),
                plugin.configYml.getInt("gui.close.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.close.item")))
                        .setDisplayName(plugin.configYml.getString("gui.close.name"))
                        .build()
                ) {
                    onLeftClick { event, _ -> event.whoClicked.closeInventory() }
                }
            )

            for (config in plugin.configYml.getSubsections("gui.custom-slots")) {
                setSlot(
                    config.getInt("row"),
                    config.getInt("column"),
                    ConfigSlot(config)
                )
            }
        }
    }

    @JvmStatic
    fun open(player: Player) {
        menu.open(player)
    }
}
