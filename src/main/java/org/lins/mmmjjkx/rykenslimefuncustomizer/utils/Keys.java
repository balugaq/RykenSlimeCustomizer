package org.lins.mmmjjkx.rykenslimefuncustomizer.utils;

import org.bukkit.NamespacedKey;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

public class Keys {
    public static NamespacedKey newKey(String key) {
        return new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, key.toLowerCase());
    }
}
