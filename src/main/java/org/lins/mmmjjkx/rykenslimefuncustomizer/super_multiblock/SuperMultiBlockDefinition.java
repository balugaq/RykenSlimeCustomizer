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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SuperMultiBlockDefinition {
    private final MultiBlockPart core;
    private final Map<Vector3i, MultiBlockPart> map;

    public SuperMultiBlockDefinition(@NotNull MultiBlockPart core, @NotNull Map<Vector3i, MultiBlockPart> map) {
        this.core = core;
        this.map = Map.copyOf(map);
    }

    @NotNull
    public MultiBlockPart getCore() {
        return core;
    }

    @NotNull
    public Map<Vector3i, MultiBlockPart> getMap() {
        return map;
    }

    @NotNull
    public Set<Location> getLocations(@NotNull Location coreLocation) {
        Set<Location> locations = new HashSet<>();
        for (Vector3i offset : map.keySet()) {
            locations.add(offset.addTo(coreLocation));
        }
        return locations;
    }
}