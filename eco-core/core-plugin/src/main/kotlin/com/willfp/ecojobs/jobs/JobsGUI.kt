package com.willfp.ecojobs.jobs

import com.willfp.eco.core.gui.addCustomSlots
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.component.addComponent
import com.willfp.eco.core.gui.component.closeButton
import com.willfp.eco.core.gui.getSlotLocations
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.onRightClick
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.setMask
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.formatEco
import com.willfp.ecojobs.api.getActiveJobs
import com.willfp.ecojobs.api.canJoinJob
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.joinJob
import com.willfp.ecojobs.jobs.Jobs.getUnlockedJobs
import com.willfp.ecojobs.plugin
import kotlin.math.ceil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object JobsGUI {
    @Volatile
    private lateinit var menu: Menu

    internal fun update() {
        menu = buildMenu(null)
    }

    /**
     * Build the jobs menu for the jobs in [category], or for the jobs with no category when null.
     *
     * Keys in the category's own `gui` section replace the matching keys of the main `gui` section.
     */
    internal fun buildMenu(category: JobCategory?): Menu {
        val gui = plugin.configYml.getSubsection("gui").clone().apply {
            val overrides = category?.config?.getSubsectionOrNull("gui") ?: return@apply
            for (key in overrides.getKeys(false)) {
                set(key, overrides.get(key))
            }
        }

        val topLeftRow = gui.getInt("job-area.top-left.row")
        val topLeftColumn = gui.getInt("job-area.top-left.column")
        val bottomRightRow = gui.getInt("job-area.bottom-right.row")
        val bottomRightColumn = gui.getInt("job-area.bottom-right.column")

        val jobAreaSlots = (topLeftRow..bottomRightRow).flatMap { row ->
            (topLeftColumn..bottomRightColumn).map { column -> Pair(row, column) }
        }

        val jobIconBuilder = { player: Player, menu: Menu, index: Int ->
            val page = menu.getPage(player)

            val unlockedJobs = player.getUnlockedJobs(category)

            val pagedIndex = ((page - 1) * jobAreaSlots.size) + index

            val job = unlockedJobs.getOrNull(pagedIndex)
            job?.getIcon(player) ?: ItemStack(Material.AIR)
        }

        val pageChangeSound = PlayableSound.create(gui.getSubsection("page-change-sound"))

        return menu(gui.getInt("rows")) {
            title = gui.getString("title").formatEco()

            setMask(gui)

            if (gui.getBoolOrNull("player-info.enabled") ?: true) {
                for (location in gui.getSlotLocations("player-info")) {
                    setSlot(
                        location.row,
                        location.column,
                        slot { player, _ ->
                            val name = gui.getString("player-info.name")
                                .replace("%player%", player.displayName)
                                .formatEco(player, true)

                            val lore = if (player.getActiveJobs(category).isEmpty()) {
                                gui.getStrings("player-info.no-jobs")
                            } else {
                                gui.getStrings("player-info.has-jobs")
                                    .flatMap {
                                        if (it == "%jobs%") {
                                            player.getActiveJobs(category).flatMap { job ->
                                                job.injectPlaceholdersInto(
                                                    gui.getStrings("player-info.job-line"),
                                                    player
                                                )
                                            }
                                        } else {
                                            listOf(it)
                                        }
                                    }
                            }.formatEco(player, true)

                            ItemStackBuilder(
                                Items.lookup(
                                    gui.getString("player-info.icon")
                                        .replace("%player%", player.name)
                                )
                            )
                                .setDisplayName(name)
                                .addLoreLines(lore)
                                .build()
                        }
                    )
                }
            }

            for ((index, pair) in jobAreaSlots.withIndex()) {
                val (row, column) = pair

                setSlot(row, column, slot({ p, m -> jobIconBuilder(p, m, index) }) {
                    onLeftClick { player, _, _, menu ->
                        val page = menu.getPage(player)
                        val unlockedJobs = player.getUnlockedJobs(category)
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

                        PlayableSound.create(gui.getSubsection("job-icon.click"))?.playTo(player)
                    }

                    onRightClick { player, _, _, menu ->
                        val page = menu.getPage(player)
                        val unlockedJobs = player.getUnlockedJobs(category)
                        val pagedIndex = ((page - 1) * jobAreaSlots.size) + index

                        val job = unlockedJobs.getOrNull(pagedIndex) ?: return@onRightClick

                        if (player.hasJobActive(job)) {
                            job.leaveGUI.open(player)
                            PlayableSound.create(gui.getSubsection("job-icon.click"))?.playTo(player)
                        }
                    }
                })
            }

            addPageChanger(gui, "prev-page", PageChanger.Direction.BACKWARDS, pageChangeSound)
            addPageChanger(gui, "next-page", PageChanger.Direction.FORWARDS, pageChangeSound)

            maxPages { player ->
                ceil(
                    player.getUnlockedJobs(category).size.toDouble() / jobAreaSlots.size).toInt()
            }

            addComponent(closeButton(gui.getSubsection("close")))

            addCustomSlots(gui, "")
        }
    }

    @JvmStatic
    fun open(player: Player) {
        menu.open(player)
    }
}
