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
package org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

import lombok.Getter;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

@Getter
public class SuperMultiBlockManager {
    private static final SuperMultiBlockManager INSTANCE = new SuperMultiBlockManager();
    public static final NamespacedKey RSC_KEY = new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, "rsc_projectile");

    private final Map<Location, SuperMultiBlock> monitoringLocations = new ConcurrentHashMap<>();
    private final Set<Location> correctLocations = new CopyOnWriteArraySet<>();
    private final Map<Location, BlockDisplay> projectiles = new ConcurrentHashMap<>();
//    private final Map<Location, Interaction> interactions = new ConcurrentHashMap<>();

    private SuperMultiBlockManager() {}

    @NotNull
    public static SuperMultiBlockManager getInstance() {
        return INSTANCE;
    }

    public boolean startSuperMultiBlock(@NotNull SuperMultiBlock superMultiBlock) {
        Set<Location> locations = superMultiBlock.getLocations();
        if (locations.stream().anyMatch(monitoringLocations::containsKey)) {
            // don't block the incoming SuperMultiBlock
            return false;
        }

        // start monitoring the locations
        for (Location location : locations) {
            monitoringLocations.put(location, superMultiBlock);
        }
        checkProjectiles(superMultiBlock);
        if (superMultiBlock.getMachine().isDisplayProjectiles()) {
            addProjectiles(superMultiBlock);
        } else {
            removeProjectiles(superMultiBlock);
        }
        // generate cache
        superMultiBlock.generateCache();
        return true;
    }

    public void removeProjectiles(SuperMultiBlock smb) {
        Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
            for (Location location : smb.getLocations()) {
                removeProjectile(location);
            }
        });
    }

    public void checkProjectiles(SuperMultiBlock superMultiBlock) {
        for (Location location : superMultiBlock.getLocations()) {
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.1, 0.1, 0.1)) {
                if (!entity.getPersistentDataContainer().has(RSC_KEY, PersistentDataType.BOOLEAN)) {
                    continue;
                }
                if (entity.getType() == EntityType.BLOCK_DISPLAY) {
                    if (superMultiBlock.getMachine().isDisplayProjectiles()) {
                        projectiles.put(location, (BlockDisplay) entity);
                    } else {
                        entity.remove();
                        projectiles.remove(location);
                    }
                }
//                if (entity.getType() == EntityType.INTERACTION) {
//                    if (superMultiBlock.getMachine().isDisplayProjectiles()) {
//                        interactions.put(location, (Interaction) entity);
//                    } else {
//                        entity.remove();
//                        interactions.remove(location);
//                    }
//                }
            }
        }
    }

    public void stopSuperMultiBlock(@NotNull Location location) {
        var smb = getSuperMultiBlock(location);
        if (smb != null) {
            stopSuperMultiBlock(smb);
        }
    }

    public void stopSuperMultiBlock(@NotNull SuperMultiBlock superMultiBlock) {
        Set<Location> locations = superMultiBlock.getLocations();
        for (Location location : locations) {
            if (monitoringLocations.get(location) == superMultiBlock) {
                monitoringLocations.remove(location);
            }
        }

        removeProjectiles(superMultiBlock);
    }

    public void markDirty(@NotNull Location location) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RykenSlimefunCustomizer.INSTANCE, () -> {
            markDirty0(location);
        }, 1L);
    }

    private void markDirty0(@NotNull Location location) {
        SuperMultiBlock superMultiBlock = monitoringLocations.get(location);
        if (superMultiBlock == null) {
            return;
        }

        boolean isFormedBefore = superMultiBlock.isFullyFormedCached();
    
        if (!superMultiBlock.isFormed(location)) {
            correctLocations.remove(location);
        } else {
            correctLocations.add(location);
        }

        boolean isFormedNow = superMultiBlock.isFullyFormedCached();

        if (isFormedBefore && !isFormedNow) {
            superMultiBlock.onUnformed();
            stopSuperMultiBlock(superMultiBlock);
        }

        if (!isFormedBefore && isFormedNow) {
            superMultiBlock.onFormed();
            if (superMultiBlock.getMachine().isDisplayProjectiles()) {
                removeProjectiles(superMultiBlock);
            }
        }
    }

    @Nullable
    public SuperMultiBlock getSuperMultiBlock(@NotNull Location location) {
        return monitoringLocations.get(location);
    }

    public void addProjectiles(@NotNull SuperMultiBlock superMultiBlock) {
        Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
            Set<Location> locations = superMultiBlock.getLocations();
            for (Location location : locations) {
                MultiBlockPart part = superMultiBlock.getPart(location);
                if (part == null) {
                    continue;
                }

                BlockData blockData = part.getBlockData(superMultiBlock, location);
                if (blockData != null) {
                    addProjectile(location, blockData);
                } else {
                    ExceptionHandler.handleError("无法展示超大多方块投影: 机器:" + superMultiBlock.getMachine().getId() + "，位置:" + location);
                }
            }
        });
    }

    public void addProjectile(@NotNull Location location, @NotNull BlockData blockData) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.1, 0.1, 0.1)) {
            if (entity.getType() == EntityType.BLOCK_DISPLAY) {
                if (entity.getPersistentDataContainer().has(RSC_KEY, PersistentDataType.BOOLEAN)) {
                    projectiles.put(location, (BlockDisplay) entity);
                    return;
                }
            }
//            if (entity.getType() == EntityType.INTERACTION) {
//                if (entity.getPersistentDataContainer().has(RSC_KEY, PersistentDataType.BOOLEAN)) {
//                    interactions.put(location, (Interaction) entity);
//                    return;
//                }
//            }
        }
        float scale = 0.8f;
        float offset = (1.0f - scale) / 2f;
        BlockDisplay display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(blockData);
        display.setTransformation(new Transformation(new Vector3f(offset, offset, offset), new AxisAngle4f(0, 0, 0, 0), new Vector3f(scale, scale, scale), new AxisAngle4f(0, 0, 0, 0)));
        display.getPersistentDataContainer().set(RSC_KEY, PersistentDataType.BOOLEAN, true);
        display.customName(Component.empty());
        display.setCustomNameVisible(false);
        display.setGlowing(true);
        projectiles.put(location, display);
//        Interaction interaction = (Interaction) location.getWorld().spawnEntity(location.clone().add(0.5, 0.5, 0.5), EntityType.INTERACTION);
//        interaction.setInteractionHeight(scale);
//        interaction.setInteractionWidth(scale);
//        interaction.setResponsive(true);
//        interaction.getPersistentDataContainer().set(RSC_KEY, PersistentDataType.BOOLEAN, true);
//        interactions.put(location, interaction);
    }

    public void removeProjectile(@NotNull Location location) {
        BlockDisplay display = projectiles.remove(location);
        if (display != null && !display.isDead() && display.isValid()) {
            display.remove();
        }
//        Interaction interaction = interactions.remove(location);
//        if (interaction != null && !interaction.isDead() && interaction.isValid()) {
//            interaction.remove();
//        }
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            SuperMultiBlock superMultiBlock = monitoringLocations.get(event.getClickedBlock().getLocation());
            if (superMultiBlock != null) {
                superMultiBlock.onInteract(event);
            }
        }
    }
}