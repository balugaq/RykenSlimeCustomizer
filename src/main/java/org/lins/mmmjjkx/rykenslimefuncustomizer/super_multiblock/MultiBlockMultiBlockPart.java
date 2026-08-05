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


import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class MultiBlockMultiBlockPart extends SlimefunMultiBlockPart {
    public MultiBlockMultiBlockPart(SlimefunItemStack target) {
        super(target);
    }

    @Override
    public boolean isBuilt(@NotNull SuperMultiBlock ancestor, @NotNull Location partLocation) {
        SuperMultiBlock smb = SuperMultiBlockManager.getInstance().getCoreStorage().get(partLocation);
        return smb != null && smb.isFullyFormedCached();
    }
}