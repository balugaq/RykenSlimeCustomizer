package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import lombok.Getter;
import org.bukkit.Bukkit;

public class PluginStateCache {
    @Getter
    private static final Object2BooleanOpenHashMap<String> isEnabledMap = new Object2BooleanOpenHashMap<>();

    static {
        isEnabledMap.defaultReturnValue(false);
    }

    public static void init() {
        for (var plugin : Bukkit.getPluginManager().getPlugins()) {
            isEnabledMap.put(plugin.getName(), plugin.isEnabled());
        }
    }

    public static boolean isEnabled(String plugin) {
        return isEnabledMap.getBoolean(plugin);
    }
}
