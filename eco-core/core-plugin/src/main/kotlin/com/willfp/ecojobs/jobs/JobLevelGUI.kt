package com.willfp.ecojobs.jobs

import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.ConfigSlot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.formatEco
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.plugin
import com.willfp.ecomponent.components.LevelComponent
import com.willfp.ecomponent.components.LevelState
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class JobLevelGUI(
    private val job: Job
) {
    private val menu: Menu

    init {
        val maskPattern = plugin.configYml.getStrings("level-gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("level-gui.mask.materials"))

        val progressionPattern = plugin.configYml.getStrings("level-gui.progression-slots.pattern")

        val component = object : LevelComponent() {
            override val maxLevel: Int = job.maxLevel
            override val pattern: List<String> = progressionPattern

            override fun getLevelItem(player: Player, menu: Menu, level: Int, levelState: LevelState): ItemStack {
                val key = levelState.name.lowercase().replace("_", "-")

                return ItemStackBuilder(Items.lookup(plugin.configYml.getString("level-gui.progression-slots.$key.item")))
                    .setDisplayName(
                        plugin.configYml.getFormattedString("level-gui.progression-slots.$key.name")
                            .replace("%job%", job.name)
                            .replace("%level%", level.toString())
                            .replace("%level_numeral%", NumberUtils.toNumeral(level))
                    )
                    .addLoreLines(
                        job.injectPlaceholdersInto(
                            plugin.configYml.getFormattedStrings("level-gui.progression-slots.$key.lore"),
                            player,
                            forceLevel = level
                        )
                    )
                    .build()
                    .also {
                        if (plugin.configYml.getBool("level-gui.progression-slots.level-as-amount")) {
                            if (Prerequisite.HAS_PAPER.isMet) {
                                it.setData(DataComponentTypes.MAX_STACK_SIZE, 99)
                                it.amount = level.coerceIn(1, 99)
                            } else {
                                it.amount = level.coerceIn(1, 64)
                            }
                        }
                    }
            }

            override fun getLevelState(player: Player, level: Int): LevelState {
                return when {
                    level <= player.getJobLevel(job) -> LevelState.UNLOCKED
                    level == player.getJobLevel(job) + 1 -> LevelState.IN_PROGRESS
                    else -> LevelState.LOCKED
                }
            }
        }

        fun pageButtonItem(basePath: String, state: String): ItemStack? {
            val itemKey = if (state == "active") "item" else "item-inactive"
            val materialKey = if (state == "active") "material" else "material-inactive"

            val itemString = plugin.configYml.getStringOrNull("$basePath.$itemKey")
                ?: plugin.configYml.getStringOrNull("$basePath.$materialKey")
                ?: return null

            val builder = ItemStackBuilder(Items.lookup(itemString))

            // Deprecated: use the item/item-inactive keys to set the name instead
            val name = plugin.configYml.getStringOrNull("$basePath.name-$state")
                ?: plugin.configYml.getStringOrNull("$basePath.name")
            if (name != null) {
                builder.setDisplayName(name)
            }

            return builder.build()
        }

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("level-gui.progression-slots.page-change-sound"))

        val baseTitle = (job.title ?: plugin.configYml.getString("level-gui.title"))
            .replace("%job%", job.name)

        menu = menu(plugin.configYml.getInt("level-gui.rows")) {
            title = baseTitle.formatEco()

            maxPages(component.pages)

            setMask(
                FillerMask(
                    maskItems,
                    *maskPattern
                )
            )

            addComponent(1, 1, component)

            val prevRow = plugin.configYml.getInt("level-gui.progression-slots.prev-page.location.row")
            val prevColumn = plugin.configYml.getInt("level-gui.progression-slots.prev-page.location.column")
            val nextRow = plugin.configYml.getInt("level-gui.progression-slots.next-page.location.row")
            val nextColumn = plugin.configYml.getInt("level-gui.progression-slots.next-page.location.column")

            addComponent(
                MenuLayer.LOWER,
                prevRow,
                prevColumn,
                slot(
                    pageButtonItem("level-gui.progression-slots.prev-page", "active")
                        ?: ItemStackBuilder(Items.lookup("arrow")).build()
                ) {
                    onLeftClick { player, _, _, _ -> JobsGUI.open(player) }
                }
            )

            pageButtonItem("level-gui.progression-slots.prev-page", "active")?.let { active ->
                addPageChanger(
                    PageChanger.Direction.BACKWARDS,
                    active,
                    null,
                    pageChangeSound,
                    prevRow,
                    prevColumn
                )
            }

            pageButtonItem("level-gui.progression-slots.next-page", "active")?.let { active ->
                addPageChanger(
                    PageChanger.Direction.FORWARDS,
                    active,
                    pageButtonItem("level-gui.progression-slots.next-page", "inactive"),
                    pageChangeSound,
                    nextRow,
                    nextColumn
                )
            }

            val closeEnabled = plugin.configYml.getBoolOrNull("level-gui.progression-slots.close.enabled") ?: true
            if (closeEnabled) {
                setSlot(
                    plugin.configYml.getInt("level-gui.progression-slots.close.location.row"),
                    plugin.configYml.getInt("level-gui.progression-slots.close.location.column"),
                    slot(
                        ItemStackBuilder(Items.lookup(plugin.configYml.getString("level-gui.progression-slots.close.material")))
                            .setDisplayName(plugin.configYml.getString("level-gui.progression-slots.close.name"))
                            .build()
                    ) {
                        onLeftClick { event, _ ->
                            event.whoClicked.closeInventory()
                        }
                    }
                )
            }

            for (config in plugin.configYml.getSubsections("level-gui.custom-slots")) {
                setSlot(
                    config.getInt("row"),
                    config.getInt("column"),
                    ConfigSlot(config)
                )
            }
        }
    }

    fun open(player: Player) {
        menu.open(player)
    }
}
