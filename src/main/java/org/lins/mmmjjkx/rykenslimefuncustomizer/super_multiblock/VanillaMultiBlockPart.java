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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaMultiBlockPart implements MultiBlockPart {
    private final Material material;
    private final BlockData blockData;

    public VanillaMultiBlockPart(@Nullable Material material, BlockData blockData) {
        this.material = material;
        this.blockData = blockData;
    }

    @Override
    public boolean isOfPart(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        if (material != null) {
            // only check material
            return partLocation.getBlock().getType() == material;
        }
        
        if (partLocation.getBlock().getType() != blockData.getMaterial()) {
            return false;
        }
        if (blockData != null && !partLocation.getBlock().getBlockData().matches(blockData)) {
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    public BlockData getBlockData(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        return blockData;
    }
}