package com.willfp.ecojobs.jobs

import com.willfp.eco.util.BlockUtils
import com.willfp.ecojobs.EcoJobsPlugin
import com.willfp.ecojobs.api.activeJobs
import com.willfp.ecojobs.api.giveJobExperience
import com.willfp.libreforge.events.TriggerPreProcessEvent
import com.willfp.libreforge.triggers.Triggers
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import java.util.*

class JobBlockBreakListener(
    private val plugin: EcoJobsPlugin
) : Listener {

    private var queue = LinkedList<Pair<Block, Player?>>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTriggerPreProcess(event: TriggerPreProcessEvent) {
        if (event.trigger != Triggers.MINE_BLOCK)
            return

        event.data.block?.let {
            val placedUUID = BlockUtils.whoPlaced(it) ?: return
            if (placedUUID == event.player.uniqueId)
                return

            queue.push(Pair(it, plugin.server.getPlayer(placedUUID)))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        event.blockList().forEach { queue.push(Pair(it, null)) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().forEach { queue.push(Pair(it, null)) }
    }

    fun processQueue() {
        if (queue.size == 0)
            return

        println("Queue: ${queue.size}")
        val data = queue.pop()

        (data.second ?: BlockUtils.whoPlaced(data.first)?.run { plugin.server.getPlayer(this) })?.let { blockPlayer ->
            if (blockPlayer.activeJobs.firstOrNull()?.id == "builder") {
                blockPlayer.activeJobs.firstOrNull()?.jobXpGains?.firstOrNull { it.trigger == Triggers.MINE_BLOCK }?.let { c ->
                    blockPlayer.giveJobExperience(blockPlayer.activeJobs.firstOrNull()!!, c.multiplier)
                }
            }
        }
    }
}
