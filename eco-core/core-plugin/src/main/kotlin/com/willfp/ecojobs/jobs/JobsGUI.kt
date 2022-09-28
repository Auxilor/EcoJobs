package com.willfp.ecojobs.jobs

import com.willfp.eco.core.config.updating.ConfigUpdater
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.jobs.Jobs.unlockedJobs
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

object JobsGUI {
    private lateinit var menu: Menu
    private val jobAreaSlots = mutableListOf<Pair<Int, Int>>()
    private const val pageKey = "page"

    private fun getPage(menu: Menu, player: Player): Int {
        val pages = ceil(Jobs.values()
            .filter { player.getJobLevel(it) > 0 }
            .size.toDouble() / jobAreaSlots.size).toInt()

        val page = menu.getState(player, pageKey) ?: 1

        return max(min(pages, page + 1), 1)
    }

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
        val jobInfoItemBuilder = { player: Player, _: Menu ->
            val job = player.activeJob

            if (job == null) {
                ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.job-info.no-active.item")))
                    .setDisplayName(plugin.configYml.getFormattedString("gui.job-info.no-active.name"))
                    .addLoreLines(plugin.configYml.getFormattedStrings("gui.job-info.no-active.lore"))
                    .build()
            } else {
                job.getJobInfoIcon(player)
            }
        }

        val jobIconBuilder = { player: Player, menu: Menu, index: Int ->
            val page = getPage(menu, player)

            val unlockedJobs = player.unlockedJobs

            val pagedIndex = ((page - 1) * jobAreaSlots.size) + index

            val job = unlockedJobs.getOrNull(pagedIndex)
            job?.getIcon(player) ?: ItemStack(Material.AIR)
        }

        return menu(plugin.configYml.getInt("gui.rows")) {
            setTitle(plugin.langYml.getString("menu.title"))

            setMask(
                FillerMask(
                    MaskItems.fromItemNames(plugin.configYml.getStrings("gui.mask.materials")),
                    *plugin.configYml.getStrings("gui.mask.pattern").toTypedArray()
                )
            )

            setSlot(
                plugin.configYml.getInt("gui.job-info.row"),
                plugin.configYml.getInt("gui.job-info.column"),
                slot(jobInfoItemBuilder) {
                    onLeftClick { event, _, _ ->
                        val player = event.whoClicked as Player
                        player.activeJob?.levelGUI?.open(player)
                    }
                }
            )

            for ((index, pair) in jobAreaSlots.withIndex()) {
                val (row, column) = pair

                setSlot(row, column, slot({ p, m -> jobIconBuilder(p, m, index) }) {
                    setUpdater { p, m, _ ->
                        jobIconBuilder(p, m, index)
                    }

                    onLeftClick { event, _, _ ->
                        val player = event.whoClicked as Player

                        val page = getPage(menu, player)

                        val unlockedJobs = player.unlockedJobs

                        val pagedIndex = ((page - 1) * jobAreaSlots.size) + index

                        val job = unlockedJobs.getOrNull(pagedIndex) ?: return@onLeftClick

                        if (player.activeJob != job) {
                            player.activeJob = job
                        }

                        player.playSound(
                            player.location,
                            Sound.valueOf(plugin.configYml.getString("gui.job-icon.click.sound").uppercase()),
                            1f,
                            plugin.configYml.getDouble("gui.job-icon.click.pitch").toFloat()
                        )
                    }
                })
            }

            setSlot(
                plugin.configYml.getInt("gui.prev-page.location.row"),
                plugin.configYml.getInt("gui.prev-page.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.prev-page.item")))
                        .setDisplayName(plugin.configYml.getString("gui.prev-page.name"))
                        .build()
                ) {
                    onLeftClick { event, _, menu ->
                        val player = event.whoClicked as Player
                        val page = getPage(menu, player)

                        val newPage = max(1, page - 1)

                        menu.addState(player, pageKey, newPage)
                    }
                }
            )

            setSlot(
                plugin.configYml.getInt("gui.next-page.location.row"),
                plugin.configYml.getInt("gui.next-page.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.next-page.item")))
                        .setDisplayName(plugin.configYml.getString("gui.next-page.name"))
                        .build()
                ) {
                    onLeftClick { event, _, menu ->
                        val player = event.whoClicked as Player

                        val pages = ceil(Jobs.values()
                            .filter { player.getJobLevel(it) > 0 }
                            .size.toDouble() / jobAreaSlots.size).toInt()

                        val page = getPage(menu, player)

                        val newPage = min(pages, page + 1)

                        menu.addState(player, pageKey, newPage)
                    }
                }
            )

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

            setSlot(plugin.configYml.getInt("gui.deactivate-job.location.row"),
                plugin.configYml.getInt("gui.deactivate-job.location.column"),
                slot(
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("gui.deactivate-job.item")))
                        .setDisplayName(plugin.configYml.getString("gui.deactivate-job.name"))
                        .build()
                ) {
                    onLeftClick { event, _ ->
                        val player = event.whoClicked as Player
                        player.activeJob = null
                    }
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
