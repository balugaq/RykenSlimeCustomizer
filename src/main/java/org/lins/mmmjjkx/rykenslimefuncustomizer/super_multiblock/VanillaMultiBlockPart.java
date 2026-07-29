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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VanillaMultiBlockPart implements MultiBlockPart {
    private final BlockData blockData;
    private final DisplayDescriptor descriptor;

    public VanillaMultiBlockPart(BlockData blockData, DisplayDescriptor descriptor) {
        this.blockData = blockData;
        this.descriptor = descriptor;
    }

    @Override
    public boolean isOfPart(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        return partLocation.getBlock().getBlockData().matches(blockData);
    }

    @Override
    @Nullable
    public DisplayDescriptor getDisplayDescriptor(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        return descriptor;
    }
}