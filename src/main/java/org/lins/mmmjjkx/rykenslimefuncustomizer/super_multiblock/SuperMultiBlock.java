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

import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomSuperMultiBlockMachine;

import lombok.Getter;

@Getter
public class SuperMultiBlock {
    private final CustomSuperMultiBlockMachine machine;
    private final Location coreLocation;
    private final int minY;
    private final int maxY;
    private final int[] layers;

    public int getCoreLayerIndex() {
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] == getCoreLocation().getBlockY()) {
                return i;
            }
        }
        throw new AssertionError("Core location is not in the layers.");
    }

    public int getCurrentLayerIndex() {
        return getMachine().getCurrentLayerIndex(this);
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public int layerCount() {
        return getLayers().length;
    }

    public SuperMultiBlock(@NotNull CustomSuperMultiBlockMachine machine, @NotNull Location coreLocation) {
        this.machine = machine;
        this.coreLocation = coreLocation;
        var layers = new IntArraySet();
        int minY_ = 9999, maxY_ = -9999;
        for (Location location : getLocations()) {
            minY_ = Math.min(minY_, location.getBlockY());
            maxY_ = Math.max(maxY_, location.getBlockY());
            layers.add(location.getBlockY());
        }
        this.minY = minY_;
        this.maxY = maxY_;
        this.layers = layers.intStream().sorted().toArray();
    }

    @NotNull
    public SuperMultiBlockDefinition getDefinition() {
        return machine.getDefinition();
    }

    public boolean isFullyFormedCached() {
        return getDefinition().isFullyFormedCached(coreLocation);
    }

    public boolean isLayerFormed(int layer) {
        return getLocations().stream().allMatch(l -> l.getBlockY() != layer || l.getBlockY() == layer && isFormedCached(l));
    }

    public void generateCache() {
        for (Location location : getLocations()) {
            if (isFormed(location)) {
                SuperMultiBlockManager.getInstance().getCorrectLocations().add(location);
            }
        }
    }

    public boolean isFormedCached(Location location) {
        return SuperMultiBlockManager.getInstance().getCorrectLocations().contains(location);
    }

    public boolean isFormed(Location location) {
        var part = getPart(location);
        return part != null && part.isOfPart(this, location) && part.isBuilt(this, location);
    }

    @Nullable
    public MultiBlockPart getPart(@NotNull Location location) {
        if (location.equals(coreLocation)) {
            return getDefinition().getCore();
        }
        Vector3i offset = new Vector3i(location.toVector().subtract(coreLocation.toVector()));
        return getDefinition().getMap().get(offset);
    }

    @NotNull
    public Set<Location> getLocations() {
        return getDefinition().getLocations(coreLocation);
    }

    public void onFormed(Location location) {
        machine.onFormed(location);
    }

    public void onUnformed(Location location) {
        machine.onUnformed(location);
    }

    public void onDestroy() {
        machine.onDestroy();
    }

    public void autoSwitchedNewLayer(int layer) {
        machine.autoSwitchedNewLayer(layer);
    }

    public void formedLayer(int layer) {
        machine.formedLayer(layer);
    }

    public void onInteract(PlayerInteractEvent event) {
        // checked permission
        machine.onInteract(event, this);
    }
}