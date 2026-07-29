package org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

public interface DisplayDescriptor {
    @NotNull Display createDisplay(Location location);
}
