package org.lins.mmmjjkx.rykenslimefuncustomizer.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class AddonEnableEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ProjectAddon addon;

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
