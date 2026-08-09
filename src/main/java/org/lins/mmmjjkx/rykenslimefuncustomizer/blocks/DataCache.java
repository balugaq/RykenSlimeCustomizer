package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;

import javax.annotation.Nullable;

@NullMarked
public interface DataCache {
    Long2ObjectOpenHashMap<Object> cache = new Long2ObjectOpenHashMap<>();

    @Nullable
    default <T> T getCache(Location accessor, Access<T> access) {
        long k = access.getId() | ((long) accessor.hashCode() << 32);
        var v = cache.get(k);
        return v == null ? null : access.cast().cast(v);
    }

    default <T> void setCache(Location accessor, Access<T> access, T value) {
        long k = access.getId() | ((long) accessor.hashCode() << 32);
        cache.put(k, value);
    }
}
