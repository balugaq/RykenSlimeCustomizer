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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.mocks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class WrappedToolUseEvent extends BlockBreakEvent {
    private final Player wrapped;

    public WrappedToolUseEvent(@NotNull Block theBlock, @NotNull Player player) {
        super(theBlock, player);

        this.wrapped = MockObject.mockOne(player);
    }

    public WrappedToolUseEvent(@NotNull BlockBreakEvent originalEvent) {
        super(originalEvent.getBlock(), originalEvent.getPlayer());

        this.wrapped = MockObject.mockOne(originalEvent.getPlayer());
    }

    @Override
    public @NotNull Player getPlayer() {
        return wrapped;
    }

    @Override
    public boolean callEvent() {
        throw new UnsupportedOperationException();
    }
}
