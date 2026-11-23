package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.getJobLevel
import com.willfp.ecojobs.plugin
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import wiki.ecohub.game.item.Items
import wiki.ecohub.game.item.builder.ItemStackBuilder
import wiki.ecohub.game.item.editItemMeta
import wiki.ecohub.game.sound.PlayableSound
import wiki.ecohub.math.NumberUtils
import wiki.ecohub.modules.menus.addCustomSlots
import wiki.ecohub.modules.menus.addPageChanger
import wiki.ecohub.modules.menus.component.ConfigButton
import wiki.ecohub.modules.menus.component.LevelComponent
import wiki.ecohub.modules.menus.component.LevelState
import wiki.ecohub.modules.menus.component.addComponent
import wiki.ecohub.modules.menus.component.closeButton
import wiki.ecohub.modules.menus.menu
import wiki.ecohub.modules.menus.menu.Menu
import wiki.ecohub.modules.menus.menu.MenuLayer
import wiki.ecohub.modules.menus.page.PageChanger
import wiki.ecohub.modules.menus.setMask
import wiki.ecohub.ui.text.formatEco

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
                    .also { item ->
                        if (plugin.configYml.getBool("level-gui.progression-slots.level-as-amount")) {
                            item.editItemMeta<ItemMeta> { meta -> meta.setMaxStackSize(99) }
                            item.amount = level.coerceIn(1, 99)
                        }
                    }
            }

            override fun getLevelState(player: Player, level: Int): LevelState =
                LevelState.of(level, player.getJobLevel(job))
        }

        val pageChangeSound = PlayableSound.create(plugin.configYml.getSubsection("level-gui.progression-slots.page-change-sound"))

        val baseTitle = (job.title ?: plugin.configYml.getString("level-gui.title"))
            .replace("%job%", job.name)

        menu = menu(plugin.configYml.getInt("level-gui.rows")) {
            setTitle(baseTitle.formatEco())

            maxPages(component.pages)

            setMask(plugin.configYml, "level-gui")

            addComponent(1, 1, component)

            addComponent(
                ConfigButton(
                    plugin.configYml.getSubsection("level-gui.progression-slots.prev-page"),
                    defaultItem = "arrow",
                    layer = MenuLayer.LOWER
                ) { player, _, _, _ -> JobsGUI.open(player) }
            )

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
