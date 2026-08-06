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

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.HorizonDirection;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class SuperMultiBlockDefinition extends SuperMultiBlockManager {
    private final Map<String, MultiBlockPart> mapping;
    private final Map<Vector3i, MultiBlockPart> map;
    private final EnumMap<HorizonDirection, Map<Vector3i, MultiBlockPart>> rotatedMap = new EnumMap<>(HorizonDirection.class);

    public SuperMultiBlockDefinition(@NotNull Map<String, MultiBlockPart> mapping, @NotNull Map<Vector3i, MultiBlockPart> map) {
        this.mapping = Map.copyOf(mapping);
        this.map = Map.copyOf(map);
    }

    public int count(@NotNull String mappingName) {
        var part = getPart(mappingName);
        if (part == null) return 0;
        return Math.toIntExact(map.values().stream().filter(p -> p == part).count());
    }

    public @Nullable MultiBlockPart getPart(String mappingName) {
        return mapping.get(mappingName);
    }

    @Nullable
    public Vector findFirstValue(@NotNull HorizonDirection direction, @NotNull String mappingName) {
        var part = getPart(mappingName);
        if (part == null) return null;
        for (var e : getMap(direction).entrySet()) {
            if (e.getValue() == part) {
                return e.getKey().toVector();
            }
        }
        return null;
    }

    @NotNull
    public Map<Vector3i, MultiBlockPart> getOriginMap() {
        return map;
    }

    @NotNull
    public Map<Vector3i, MultiBlockPart> getMap(@NotNull HorizonDirection direction) {
        if (rotatedMap.containsKey(direction)) return rotatedMap.get(direction);

        var rotated = switch (direction) {
            case NORTH -> map;
            case EAST -> map.entrySet().stream().collect(Collectors.toMap(
                e -> new Vector3i(-e.getKey().z, e.getKey().y, e.getKey().x),  // 90°
                Map.Entry::getValue,
                (v1, v2) -> v1  // 合并策略：如果冲突保留第一个
            ));
            case SOUTH -> map.entrySet().stream().collect(Collectors.toMap(
                e -> new Vector3i(-e.getKey().x, e.getKey().y, -e.getKey().z), // 180°
                Map.Entry::getValue,
                (v1, v2) -> v1
            ));
            case WEST -> map.entrySet().stream().collect(Collectors.toMap(
                e -> new Vector3i(e.getKey().z, e.getKey().y, -e.getKey().x),  // 270°
                Map.Entry::getValue,
                (v1, v2) -> v1
            ));
        };
        rotatedMap.put(direction, rotated);
        return rotated;
    }

    @NotNull
    public Set<Location> getLocations(@NotNull Location coreLocation, HorizonDirection direction) {
        Set<Location> locations = new HashSet<>();
        for (Vector3i offset : getMap(direction).keySet()) {
            locations.add(offset.addTo(coreLocation));
        }
        return locations;
    }

    public boolean isFullyFormedCached(Location coreLocation, HorizonDirection direction) {
        return getCorrectLocations().containsAll(getLocations(coreLocation, direction));
    }
}