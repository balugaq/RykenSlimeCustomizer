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

    public SuperMultiBlock(@NotNull CustomSuperMultiBlockMachine machine, @NotNull Location coreLocation) {
        this.machine = machine;
        this.coreLocation = coreLocation;
    }

    @NotNull
    public SuperMultiBlockDefinition getDefinition() {
        return machine.getDefinition();
    }

    public boolean isFullyFormedCached() {
        return getDefinition().isFullyFormedCached(coreLocation);
    }

    public void generateCache() {
        for (Location location : getLocations()) {
            if (isFormed(location)) {
                SuperMultiBlockManager.getInstance().getCorrectLocations().add(location);
            }
        }
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

    public void onFormed() {
        machine.onFormed();
    }

    public void onUnformed() {
        machine.onUnformed();
    }

    public void onInteract(PlayerInteractEvent event) {
        machine.onInteract(event, this);
    }
}