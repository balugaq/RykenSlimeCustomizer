package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.atomic.AtomicInteger;

@NullMarked
@FunctionalInterface
public interface Access<T> {
    Object2IntOpenHashMap<Access<?>> idMap = new Object2IntOpenHashMap<>();
    AtomicInteger id = new AtomicInteger(0);

    default int getId() {
        idMap.putIfAbsent(this, id.getAndIncrement());
        return idMap.getInt(this);
    }

    Class<T> cast();
}
