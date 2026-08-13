package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;

import javax.annotation.Nullable;

@NullMarked
public interface DataCache {
    Long2ObjectOpenHashMap<Object> cache = new Long2ObjectOpenHashMap<>();

    @Nullable
    default <T> T getCache(Location accessor, CacheAccess<T> cacheAccess) {
        long k = cacheAccess.getId() | ((long) accessor.hashCode() << 32);
        var v = cache.get(k);
        return v == null ? null : cacheAccess.cast().cast(v);
    }

    default <T> void setCache(Location accessor, CacheAccess<T> cacheAccess, T value) {
        long k = cacheAccess.getId() | ((long) accessor.hashCode() << 32);
        cache.put(k, value);
    }
}
