package com.willfp.ecojobs.jobs

import com.willfp.ecojobs.api.hasJobActive
import com.willfp.ecojobs.api.leaveJob
import com.willfp.ecojobs.plugin
import org.bukkit.entity.Player
import wiki.ecohub.game.item.Items
import wiki.ecohub.game.item.builder.ItemStackBuilder
import wiki.ecohub.modules.menus.getSlotLocations
import wiki.ecohub.modules.menus.menu
import wiki.ecohub.modules.menus.onLeftClick
import wiki.ecohub.modules.menus.setMask
import wiki.ecohub.modules.menus.slot
import wiki.ecohub.ui.text.formatEco

class JobLeaveGUI(
    job: Job
) {
    private val menu = menu(plugin.configYml.getInt("leave-gui.rows")) {
        setTitle(
            (job.title ?: plugin.configYml.getString("leave-gui.title"))
                .replace("%job%", job.name)
                .formatEco()
        )

        setMask(plugin.configYml, "leave-gui")

        for (location in plugin.configYml.getSlotLocations("leave-gui.cancel")) {
            setSlot(
                row = location.row,
                column = location.column,
                slot = slot({ player, _ ->
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
        }

        for (location in plugin.configYml.getSlotLocations("leave-gui.confirm")) {
            setSlot(
                row = location.row,
                column = location.column,
                slot = slot({ player, _ ->
                    ItemStackBuilder(Items.lookup(plugin.configYml.getString("leave-gui.confirm.item")))
                        .setDisplayName(plugin.configYml.getString("leave-gui.confirm.name").replace("%job%", job.name))
                        .addLoreLines(
                            job.injectPlaceholdersInto(plugin.configYml.getStrings("leave-gui.confirm.lore"), player)
                        )
                        .build()
                }) {
                    onLeftClick { player, _, _, _ ->
                        player.leaveJob(job)

                        if (!player.hasJobActive(job)) {
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
    }

    fun open(player: Player) {
        menu.open(player)
    }
}
