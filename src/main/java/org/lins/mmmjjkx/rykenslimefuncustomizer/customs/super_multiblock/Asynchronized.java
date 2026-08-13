package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.super_multiblock;

import org.bukkit.Bukkit;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

public interface Asynchronized {
    default void runAsyncLater(Runnable runnable) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(RykenSlimefunCustomizer.INSTANCE, runnable, 1L);
    }
}
