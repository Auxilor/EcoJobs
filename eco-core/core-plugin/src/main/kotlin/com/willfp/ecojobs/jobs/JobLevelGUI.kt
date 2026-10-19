package com.willfp.ecojobs.jobs

import com.willfp.eco.core.Prerequisite
import com.willfp.eco.core.gui.addCustomSlots
import com.willfp.eco.core.gui.addPageChanger
import com.willfp.eco.core.gui.component.LevelComponent
import com.willfp.eco.core.gui.component.LevelState
import com.willfp.eco.core.gui.component.addComponent
import com.willfp.eco.core.gui.component.closeButton
import com.willfp.eco.core.gui.getSlotLocations
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.menu.Menu
import com.willfp.eco.core.gui.menu.MenuLayer
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.page.PageChanger
import com.willfp.eco.core.gui.setMask
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.sound.PlayableSound
import com.willfp.eco.util.NumberUtils
import com.willfp.eco.util.formatEco
import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.plugin
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class JobLevelGUI(
    private val job: Job
) {
    private val menu: Menu

    init {
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
                return LevelState.of(level, player.getJobLevel(job))
            }
        }

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("level-gui.progression-slots.page-change-sound"))

        val baseTitle = (job.title ?: plugin.configYml.getString("level-gui.title"))
            .replace("%job%", job.name)

        menu = menu(plugin.configYml.getInt("level-gui.rows")) {
            title = baseTitle.formatEco()

            maxPages(component.pages)

            setMask(plugin.configYml, "level-gui")

            addComponent(1, 1, component)

            for (location in plugin.configYml.getSlotLocations("level-gui.progression-slots.prev-page")) {
                addComponent(
                    MenuLayer.LOWER,
                    location.row,
                    location.column,
                    slot(
                        plugin.configYml.getStringOrNull("level-gui.progression-slots.prev-page.item")
                            ?.let { Items.lookup(it).item }
                            ?: ItemStackBuilder(Items.lookup("arrow")).build()
                    ) {
                        onLeftClick { player, _, _, _ -> JobsGUI.open(player) }
                    }
                )
            }

            addPageChanger(plugin.configYml, "level-gui.progression-slots.prev-page", PageChanger.Direction.BACKWARDS, pageChangeSound)
            addPageChanger(plugin.configYml, "level-gui.progression-slots.next-page", PageChanger.Direction.FORWARDS, pageChangeSound)

            addComponent(closeButton(plugin.configYml.getSubsection("level-gui.progression-slots.close")))

            addCustomSlots(plugin.configYml, "level-gui")
        }
    }

    fun open(player: Player) {
        menu.open(player)
    }
}
