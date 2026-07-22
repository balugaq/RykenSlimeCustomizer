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

@Getter
public class SuperMultiBlockManager {
    private static final SuperMultiBlockManager INSTANCE = new SuperMultiBlockManager();
    private static final NamespacedKey RSC_KEY = new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, "rsc_projectile");

    private final Map<Location, SuperMultiBlock> monitoringLocations = new ConcurrentHashMap<>();
    private final Set<Location> correctLocations = new CopyOnWriteArraySet<>();
    private final Map<Location, BlockDisplay> projectiles = new ConcurrentHashMap<>();

    private SuperMultiBlockManager() {}

    @NotNull
    public static SuperMultiBlockManager getInstance() {
        return INSTANCE;
    }

    public boolean startSuperMultiBlock(@NotNull SuperMultiBlock superMultiBlock) {
        Set<Location> locations = superMultiBlock.getLocations();
        if (locations.stream().anyMatch(location -> monitoringLocations.containsKey(location))) {
            // don't block the incoming SuperMultiBlock
            return false;
        }

        // start monitoring the locations
        for (Location location : locations) {
            monitoringLocations.put(location, superMultiBlock);
        }
        if (superMultiBlock.getMachine().isDisplayProjectiles()) {
            displayProjectiles(superMultiBlock);
        }
        // generate cache
        superMultiBlock.generateCache();
        return true;
    }

    public void checkProjectiles(SuperMultiBlock superMultiBlock) {
        for (Location location : superMultiBlock.getLocations()) {
            for (Entity entity : location.getWorld().getNearbyEntities(location, 0.1, 0.1, 0.1)) {
                if (entity.getType() == EntityType.BLOCK_DISPLAY) {
                    if (entity.getPersistentDataContainer().has(RSC_KEY, PersistentDataType.BOOLEAN)) {
                        if (superMultiBlock.getMachine().isDisplayProjectiles()) {
                            projectiles.put(location, (BlockDisplay) entity);
                        } else {
                            entity.remove();
                            projectiles.remove(location);
                        }
                    }
                }
            }
        }
    }

    public void stopSuperMultiBlock(@NotNull SuperMultiBlock superMultiBlock) {
        Set<Location> locations = superMultiBlock.getLocations();
        for (Location location : locations) {
            if (monitoringLocations.get(location) == superMultiBlock) {
                monitoringLocations.remove(location);
            }
        }
    }

    public void markDirty(@NotNull Location location) {
        SuperMultiBlock superMultiBlock = monitoringLocations.get(location);
        if (superMultiBlock == null) {
            return;
        }

        boolean isFormedBefore = superMultiBlock.isFullyFormed();
    
        if (!superMultiBlock.isFormed(location)) {
            correctLocations.remove(location);
        }

        boolean isFormedNow = superMultiBlock.isFullyFormed();

        if (isFormedBefore && !isFormedNow) {
            superMultiBlock.onUnformed();
            stopSuperMultiBlock(superMultiBlock);
        }

        if (!isFormedBefore && isFormedNow) {
            superMultiBlock.onFormed();
        }
    }

    @Nullable
    public SuperMultiBlock getSuperMultiBlock(@NotNull Location location) {
        return monitoringLocations.get(location);
    }

    @NotNull
    public Map<Location, BlockDisplay> getProjectiles() {
        return projectiles;
    }

    public void displayProjectiles(@NotNull SuperMultiBlock superMultiBlock) {
        Set<Location> locations = superMultiBlock.getLocations();
        for (Location location : locations) {
            MultiBlockPart part = superMultiBlock.getPart(location);
            if (part != null) {
                BlockData blockData = part.getBlockData(superMultiBlock, location);
                if (blockData != null) {
                    addProjectile(location, blockData);
                }
            }
        }
    }

    public void addProjectile(@NotNull Location location, @NotNull BlockData blockData) {
        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.1, 0.1, 0.1)) {
            if (entity.getType() == EntityType.BLOCK_DISPLAY) {
                if (entity.getPersistentDataContainer().has(RSC_KEY, PersistentDataType.BOOLEAN)) {
                    projectiles.put(location, (BlockDisplay) entity);
                    return;
                }
            }
        }
        BlockDisplay display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(blockData);
        display.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 0), new Vector3f(0.8f, 0.8f, 0.8f), new AxisAngle4f(0, 0, 0, 0)));
        display.getPersistentDataContainer().set(RSC_KEY, PersistentDataType.BOOLEAN, true);
        display.customName(Component.empty());
        display.setCustomNameVisible(false);
        projectiles.put(location, display);
    }

    public void removeProjectile(@NotNull Location location) {
        BlockDisplay display = projectiles.remove(location);
        if (display != null && !display.isDead() && display.isValid()) {
            display.remove();
        }
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