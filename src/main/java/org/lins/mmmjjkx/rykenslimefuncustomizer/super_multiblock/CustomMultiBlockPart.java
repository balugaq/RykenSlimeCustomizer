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
import org.bukkit.block.data.BlockData;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;

public class CustomMultiBlockPart implements MultiBlockPart {
    private final JavaScriptEval eval;
    private final BlockData blockData;

    public CustomMultiBlockPart(@NotNull JavaScriptEval eval, @Nullable BlockData blockData) {
        this.eval = eval;
        this.blockData = blockData;
    }

    @Override
    public boolean isOfPart(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        Value result = eval.evalFunction("isOfPart", partLocation, superMultiBlockInstance);
        return result != null && result.asBoolean();
    }

    @Override
    @Nullable
    public BlockData getBlockData(@NotNull SuperMultiBlock superMultiBlockInstance, @NotNull Location partLocation) {
        if(blockData != null) return blockData;
        Value result = eval.evalFunction("getBlockData", partLocation, superMultiBlockInstance);
        if (result == null) return null;
        return result.as(BlockData.class);
    }
}