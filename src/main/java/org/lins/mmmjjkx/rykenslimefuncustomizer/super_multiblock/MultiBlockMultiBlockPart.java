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

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiBlockMultiBlockPart extends SlimefunMultiBlockPart {
    public MultiBlockMultiBlockPart(SlimefunItemStack target) {
        super(target);
    }

    @Override
    public boolean isOfPart(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        SlimefunItem sfItem = StorageCacheUtils.getSfItem(partLocation);
        return sfItem != null && sfItem.getId().equals(target.getItemId());
    }

    @Override
    @Nullable
    public BlockData getBlockData(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        return blockData;
    }

    @Override
    public boolean isBuilt(@NotNull SuperMultiBlock ancestor, @NotNull Location partLocation) {
        SuperMultiBlock smb = SuperMultiBlockManager.getInstance().getSuperMultiBlock(partLocation);
        return smb != null && smb.isFullyFormedCached(); // 不需要检测是不是正确的多方块，因为一定经过 isOfPart 检测。
    }
}