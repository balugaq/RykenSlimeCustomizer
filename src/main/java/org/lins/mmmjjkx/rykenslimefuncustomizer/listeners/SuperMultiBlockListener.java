/*
 * RykenSlimefunCustomizer
 * Copyright (C) 2026 lijinhong11(mmmjjjkx) and balugaq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.lins.mmmjjkx.rykenslimefuncustomizer.listeners;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.papermc.paper.event.block.CompostItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jspecify.annotations.NonNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.Asynchronized;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockManager;

public class SuperMultiBlockListener extends SuperMultiBlockManager implements Listener, Asynchronized {
    public SuperMultiBlockListener() {
        Bukkit.getPluginManager().registerEvents(this, RykenSlimefunCustomizer.INSTANCE);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(@NonNull BlockPlaceEvent e) {
        markDirty(e.getBlock().getLocation());
        runAsyncLater(() -> tryModifyMenu(e.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(@NonNull BlockBreakEvent e) {
        markDirty(e.getBlock().getLocation());
        runAsyncLater(() -> getMenuModified().remove(e.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(@NonNull BlockExplodeEvent e) {
        for (var block : e.blockList()) {
            markDirty(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(@NonNull EntityExplodeEvent e) {
        for (var block : e.blockList()) {
            markDirty(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDamage(@NonNull BlockDamageEvent e) {
        markDirty(e.getBlock().getLocation());
    }

    // blockfade event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(@NonNull BlockFadeEvent e) {
        markDirty(e.getBlock().getLocation());
    }

    // cauldron event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCauldron(CauldronLevelChangeEvent event) {
        markDirty(event.getBlock().getLocation());
    }

    // compost item event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCompostItem(CompostItemEvent event) {
        markDirty(event.getBlock().getLocation());
    }

    // tnt prime event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTNTPrime(TNTPrimeEvent event) {
        markDirty(event.getBlock().getLocation());
    }

    // leaves decay event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        markDirty(event.getBlock().getLocation());
    }

    // piston extend event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent event) {
        for (var block : event.getBlocks()) {
            markDirty(block.getLocation());
        }
    }

    // piston retract event
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (var block : event.getBlocks()) {
            markDirty(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(@NonNull BlockPhysicsEvent e) {
        markDirty(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(@NonNull PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
        if (b == null) return;
        if (Slimefun.getProtectionManager().hasPermission(e.getPlayer(), b, Interaction.INTERACT_BLOCK)) {
            SuperMultiBlockManager.getInstance().onPlayerInteract(e, b);
        }
    }

    private void markDirty(@NonNull Location location) {
        markDirty(location, true);
    }
}