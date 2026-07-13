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

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jspecify.annotations.NonNull;

public class WrappedUseEvent extends PlayerRightClickEvent {
    /**
     * This constructs a new {@link PlayerRightClickEvent} based on the original {@link PlayerInteractEvent}.
     * The {@link Result} of the original {@link PlayerInteractEvent} will be copied.
     *
     * @param originalEvent The original {@link PlayerInteractEvent}
     */
    public WrappedUseEvent(@NonNull PlayerInteractEvent originalEvent) {
        super(originalEvent);

        this.player = MockObject.mockOne(originalEvent.getPlayer());
    }

    /**
     * This constructs a new wrapped {@link PlayerRightClickEvent} based on the original {@link PlayerRightClickEvent}.
     * The {@link Result} of the original {@link PlayerRightClickEvent} will be copied.
     *
     * @param originalEvent The original {@link PlayerRightClickEvent}
     */
    public WrappedUseEvent(@NonNull PlayerRightClickEvent originalEvent) {
        super(originalEvent.getInteractEvent());

        this.player = MockObject.mockOne(originalEvent.getPlayer());
    }

    @Override
    public boolean callEvent() {
        throw new UnsupportedOperationException();
    }
}
