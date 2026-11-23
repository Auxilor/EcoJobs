package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.canJoinJob
import com.willfp.ecojobs.api.getActiveJobs
import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.joinJob
import com.willfp.ecojobs.jobs.Jobs.getUnlockedJobs
import com.willfp.ecojobs.plugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import wiki.ecohub.config.Config
import wiki.ecohub.data.cache.EcoCache
import wiki.ecohub.game.item.Items
import wiki.ecohub.game.item.builder.ItemStackBuilder
import wiki.ecohub.game.sound.PlayableSound
import wiki.ecohub.modules.menus.addCustomSlots
import wiki.ecohub.modules.menus.addPageChanger
import wiki.ecohub.modules.menus.component.addComponent
import wiki.ecohub.modules.menus.component.closeButton
import wiki.ecohub.modules.menus.getSlotLocations
import wiki.ecohub.modules.menus.menu
import wiki.ecohub.modules.menus.menu.Menu
import wiki.ecohub.modules.menus.onClick
import wiki.ecohub.modules.menus.page.PageChanger
import wiki.ecohub.modules.menus.setMask
import wiki.ecohub.modules.menus.slot
import wiki.ecohub.ui.text.formatEco
import java.time.Duration
import kotlin.math.ceil

object JobsGUI {
    private val playerInfoIcons = EcoCache.builder<String, ItemStack>()
        .expireAfterWrite(Duration.ofSeconds(30))
        .build()

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
        val gui = getGuiConfig(category)

        val topLeftRow = gui.getInt("job-area.top-left.row")
        val topLeftColumn = gui.getInt("job-area.top-left.column")
        val bottomRightRow = gui.getInt("job-area.bottom-right.row")
        val bottomRightColumn = gui.getInt("job-area.bottom-right.column")

        val jobAreaSlots = (topLeftRow..bottomRightRow).flatMap { row ->
            (topLeftColumn..bottomRightColumn).map { column -> Pair(row, column) }
        }

        val leaveClickName = gui.getString("job-icon.leave-click").uppercase().replace("-", "_")
        val leaveClick = ClickType.entries.firstOrNull { it.name == leaveClickName } ?: ClickType.RIGHT
        val openClick = if (leaveClick == ClickType.LEFT) ClickType.RIGHT else ClickType.LEFT

        val pageChangeSound = PlayableSound.create(gui.getSubsection("page-change-sound"))

        return menu(gui.getInt("rows")) {
            setTitle(gui.getString("title").formatEco())

            setMask(gui)

            if (gui.getBoolOrNull("player-info.enabled") != false) {
                for (location in gui.getSlotLocations("player-info")) {
                    setSlot(
                        row = location.row,
                        column = location.column,
                        slot = slot { player, _ -> getPlayerInfo(gui, category, player) }
                    )
                }
            }

            for ((index, pair) in jobAreaSlots.withIndex()) {
                val (row, column) = pair

                setSlot(row, column, slot({ player, menu ->
                    getJobAt(player, menu, category, jobAreaSlots.size, index)?.getIcon(player)
                        ?: ItemStack(Material.AIR)
                }) {
                    onClick(openClick) { player, _, _, menu ->
                        getJobAt(player, menu, category, jobAreaSlots.size, index)?.let { job ->
                            openJob(gui, player, job)
                        }
                    }

                    onClick(leaveClick) { player, _, _, menu ->
                        getJobAt(player, menu, category, jobAreaSlots.size, index)?.let { job ->
                            openLeaveMenu(gui, player, job)
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

    private fun getGuiConfig(category: JobCategory?): Config {
        val gui = plugin.configYml.getSubsection("gui").clone()
        val overrides = category?.run { config.getSubsectionOrNull("gui") } ?: return gui
        for (key in overrides.getKeys(false)) {
            gui[key] = overrides[key]
        }
        return gui
    }

    private fun getJobAt(player: Player, menu: Menu, category: JobCategory?, slotsPerPage: Int, index: Int): Job? {
        val pagedIndex = (menu.getPage(player) - 1) * slotsPerPage + index
        return player.getUnlockedJobs(category).getOrNull(pagedIndex)
    }

    private fun getPlayerInfo(gui: Config, category: JobCategory?, player: Player): ItemStack {
        val name = gui.getString("player-info.name")
            .replace("%player%", player.displayName)
            .formatEco(player, true)

        val lore = if (player.getActiveJobs(category).isEmpty()) {
            gui.getStrings("player-info.no-jobs")
        } else {
            gui.getStrings("player-info.has-jobs")
                .flatMap { line ->
                    if (line == "%jobs%") {
                        player.getActiveJobs(category).flatMap { job ->
                            job.injectPlaceholdersInto(
                                gui.getStrings("player-info.job-line"),
                                player
                            )
                        }
                    } else {
                        listOf(line)
                    }
                }
        }.formatEco(player, true)

        val icon = gui.getString("player-info.icon").replace("%player%", player.name)

        return ItemStackBuilder(playerInfoIcons.get(icon) { Items.lookup(it).getItem() }.clone())
            .setDisplayName(name)
            .addLoreLines(lore)
            .build()
    }

    private fun openJob(gui: Config, player: Player, job: Job) {
        when {
            player.hasJobActive(job) -> {
                job.levelGUI.open(player)
            }
            player.canJoinJob(job) -> {
                player.joinJob(job)

                if (player.hasJobActive(job)) {
                    player.sendMessage(
                        plugin.langYml.getMessage("joined-job")
                            .replace("%job%", job.name)
                    )
                }
            }
            else -> {
                player.sendMessage(plugin.langYml.getMessage("cannot-join-job"))
                return
            }
        }

        PlayableSound.create(gui.getSubsection("job-icon.click"))?.playTo(player)
    }

    private fun openLeaveMenu(gui: Config, player: Player, job: Job) {
        if (player.hasJobActive(job)) {
            job.leaveGUI.open(player)
            PlayableSound.create(gui.getSubsection("job-icon.click"))?.playTo(player)
        }
    }

    @JvmStatic
    fun open(player: Player) {
        menu.open(player)
    }
}
