package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.super_multiblock;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.jspecify.annotations.NonNull;

public interface DisplayDescriptor {
    @NonNull Display createDisplay(Location location);
}
