package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.atomic.AtomicInteger;

@NullMarked
@FunctionalInterface
public interface CacheAccess<T> {
    Object2IntOpenHashMap<CacheAccess<?>> idMap = new Object2IntOpenHashMap<>();
    AtomicInteger id = new AtomicInteger(0);

    default int getId() {
        idMap.putIfAbsent(this, id.getAndIncrement());
        return idMap.getInt(this);
    }

    Class<T> cast();
}
