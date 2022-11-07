package com.willfp.ecojobs.jobs

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.gui.menu
import com.willfp.eco.core.gui.onLeftClick
import com.willfp.eco.core.gui.slot
import com.willfp.eco.core.gui.slot.FillerMask
import com.willfp.eco.core.gui.slot.MaskItems
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.util.formatEco
import org.bukkit.entity.Player

class JobLeaveGUI(
    plugin: EcoPlugin,
    job: Job
) {
    private val menu = menu(plugin.configYml.getInt("leave-gui.rows")) {
        val maskPattern = plugin.configYml.getStrings("leave-gui.mask.pattern").toTypedArray()
        val maskItems = MaskItems.fromItemNames(plugin.configYml.getStrings("leave-gui.mask.materials"))

        title = plugin.configYml.getString("leave-gui.title")
            .replace("%job%", job.name)
            .formatEco()

        setMask(
            FillerMask(
                maskItems,
                *maskPattern
            )
        )

        setSlot(
            plugin.configYml.getInt("leave-gui.cancel.location.row"),
            plugin.configYml.getInt("leave-gui.cancel.location.column"),
            slot({ player, _ ->
                ItemStackBuilder(Items.lookup(plugin.configYml.getString("leave-gui.cancel.item")))
                    .setDisplayName(plugin.configYml.getString("leave-gui.cancel.name").replace("%job%", job.name))
                    .addLoreLines(
                        job.injectPlaceholdersInto(plugin.configYml.getStrings("leave-gui.cancel.lore"), player)
                    )
                    .build()
            }) {
                onLeftClick { player, _, _, _ ->
                    JobsGUI.open(player)
                }
            }
        )

        setSlot(
            plugin.configYml.getInt("leave-gui.confirm.location.row"),
            plugin.configYml.getInt("leave-gui.confirm.location.column"),
            slot({ player, _ ->
                ItemStackBuilder(Items.lookup(plugin.configYml.getString("leave-gui.confirm.item")))
                    .setDisplayName(plugin.configYml.getString("leave-gui.confirm.name").replace("%job%", job.name))
                    .addLoreLines(
                        job.injectPlaceholdersInto(plugin.configYml.getStrings("leave-gui.confirm.lore"), player)
                    )
                    .build()
            }) {
                onLeftClick { player, _, _, _ ->
                    player.activeJob = null

                    if (player.activeJob == null) {
                        player.sendMessage(
                            plugin.langYml.getMessage("left-job")
                                .replace("%job%", job.name)
                        )
                    } else {
                        player.sendMessage(
                            plugin.langYml.getMessage("cant-leave-job")
                                .replace("%job%", job.name)
                        )
                    }

                    JobsGUI.open(player)
                }
            }
        )
    }

    fun open(player: Player) {
        menu.open(player)
    }
}
