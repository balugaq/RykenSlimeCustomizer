package org.lins.mmmjjkx.rykenslimefuncustomizer.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddonLoader;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class AddonLoadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ProjectAddonLoader loader;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
