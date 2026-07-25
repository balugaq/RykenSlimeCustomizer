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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Consumer;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

import lombok.Getter;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomSuperMultiBlockMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

@Getter
public class SuperMultiBlockManager {
    private static final SuperMultiBlockManager INSTANCE = new SuperMultiBlockManager();
    public static final NamespacedKey RSC_KEY = new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, "rsc_projectile");

    private final Map<Location, SuperMultiBlock> monitoringLocations = new ConcurrentHashMap<>();
    private final Set<Location> correctLocations = new CopyOnWriteArraySet<>();
    private final Map<Location, BlockDisplay> projectiles = new ConcurrentHashMap<>();
    public static final float DEFAULT_DISPLAY_SCALE = 0.8f;
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
        Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
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
        });
    }

    public void destroySuperMultiBlock(@NotNull Location location) {
        var smb = getSuperMultiBlock(location);
        if (smb != null) {
            destroySuperMultiBlock(smb);
        }
    }

    public void destroySuperMultiBlock(@NotNull SuperMultiBlock superMultiBlock) {
        Set<Location> locations = superMultiBlock.getLocations();
        for (Location location : locations) {
            if (monitoringLocations.get(location) == superMultiBlock) {
                monitoringLocations.remove(location);
            }
        }

        removeProjectiles(superMultiBlock);
        CustomSuperMultiBlockMachine.firstTicks.remove(superMultiBlock.getCoreLocation());
        superMultiBlock.onDestroy();
    }

    public void markDirty(@NotNull Location location, boolean autoSwitchLayer) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RykenSlimefunCustomizer.INSTANCE, () -> {
            markDirty0(location, autoSwitchLayer);
        }, 1L);
    }

    private void markDirty0(@NotNull Location location, boolean autoSwitchLayer) {
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
        if (!isFormedNow && autoSwitchLayer) {
            // try switch layer
            int layerIndex = superMultiBlock.getMachine().getCurrentLayerIndex(superMultiBlock);
            int layer = superMultiBlock.getLayers()[layerIndex];
            if (superMultiBlock.isLayerFormed(layer)) {
                superMultiBlock.formedLayer(layer);
                if (switchToUnformedLayer(superMultiBlock)) {
                    if (superMultiBlock.getMachine().isDefaultNotice()) {
                        findNearbyPlayers(location, 10, p -> {
                            p.sendMessage(CMIChatColor.colorize("&c已搭建完成 y=" + layer));
                        });
                    }
                }
            }
        }

        if (isFormedBefore && !isFormedNow) {
            superMultiBlock.onUnformed(location);
            destroySuperMultiBlock(superMultiBlock);
        }

        if (!isFormedBefore && isFormedNow) {
            superMultiBlock.onFormed(location);
            if (superMultiBlock.getMachine().isDisplayProjectiles()) {
                removeProjectiles(superMultiBlock);
            }
        }
    }

    public static void findNearbyPlayers(@NotNull Location location, double radius, Consumer<Player> consumer) {
        Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
            location.getWorld().getNearbyPlayers(location, radius).forEach(consumer::accept);
        });
    }

    private boolean switchToUnformedLayer(SuperMultiBlock instance) {
        for (int layer : instance.getLayers()) {
            if (!instance.isLayerFormed(layer)) {
                instance.autoSwitchedNewLayer(layer);
                hideEntities(selectEntities(instance, instance.getLayers()[instance.getCurrentLayerIndex()]));
                showEntities(selectEntities(instance, layer));
                return true;
            }
        }
        return false;
    }

    @Nullable
    public SuperMultiBlock getSuperMultiBlock(@NotNull Location location) {
        return monitoringLocations.get(location);
    }

    public void addProjectiles(@NotNull SuperMultiBlock instance) {
        Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
            Set<Location> locations = instance.getLocations();
            for (Location location : locations) {
                MultiBlockPart part = instance.getPart(location);
                if (part == null) {
                    continue;
                }

                BlockData blockData = part.getBlockData(instance, location);
                if (blockData != null) {
                    addProjectile(location, blockData, !instance.getMachine().isAllowSwitchDisplayLayer());
                } else {
                    ExceptionHandler.handleError("无法展示超大多方块投影: 机器:" + instance.getMachine().getId() + "，位置:" + location);
                }
            }

            showEntities(selectEntities(instance, instance.getCoreLocation().getBlockY()));
            StorageCacheUtils.setData(instance.getCoreLocation(), "layer", "" + instance.getCoreLayerIndex());
        });
    }

    public void addProjectile(@NotNull Location location, @NotNull BlockData blockData, boolean visible) {
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
        BlockDisplay display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(blockData);
        // 0.0f is a hack, which means invisible.
        display.setTransformation(visible ? getTransformation(DEFAULT_DISPLAY_SCALE) : getTransformation(0.0f));
        display.getPersistentDataContainer().set(RSC_KEY, PersistentDataType.BOOLEAN, true);
        display.customName(Component.empty());
        display.setCustomNameVisible(false);
        display.setGlowing(true);
        display.setBrightness(new Display.Brightness(15, 15));
        projectiles.put(location, display);
//        Interaction interaction = (Interaction) location.getWorld().spawnEntity(location.clone().add(0.5, 0.5, 0.5), EntityType.INTERACTION);
//        interaction.setInteractionHeight(scale);
//        interaction.setInteractionWidth(scale);
//        interaction.setResponsive(true);
//        interaction.getPersistentDataContainer().set(RSC_KEY, PersistentDataType.BOOLEAN, true);
//        interactions.put(location, interaction);
    }

    public Transformation getTransformation(float scale) {
        float offset = (1.0f - scale) / 2f;
        return new Transformation(new Vector3f(offset, offset, offset), new AxisAngle4f(0, 0, 0, 0), new Vector3f(scale, scale, scale), new AxisAngle4f(0, 0, 0, 0));
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

    public void onPlayerInteract(PlayerInteractEvent event, Block b) {
        SuperMultiBlock superMultiBlock = monitoringLocations.get(b.getLocation());
        if (superMultiBlock != null) {
            superMultiBlock.onInteract(event);
        }
    }

    private Set<BlockDisplay> selectEntities(@NotNull SuperMultiBlock instance, int layer) {
        return instance.getLocations().stream().filter(l -> l.getBlockY() == layer).map(projectiles::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void showEntities(Set<BlockDisplay> entities) {
        entities.forEach(entity -> entity.setTransformation(getTransformation(DEFAULT_DISPLAY_SCALE)));
    }

    public void hideEntities(Set<BlockDisplay> entities) {
        // 0.0f is a hack, which means invisible.
        entities.forEach(entity -> entity.setTransformation(getTransformation(0.0f)));
    }

    public void updateLayer(@NotNull SuperMultiBlock instance, int oldLayer, int newLayer) {
        Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
            showEntities(selectEntities(instance, newLayer));
            hideEntities(selectEntities(instance, oldLayer));
        });
    }
}