package org.lins.mmmjjkx.rykenslimefuncustomizer.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class AddonDisableEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ProjectAddon addon;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
