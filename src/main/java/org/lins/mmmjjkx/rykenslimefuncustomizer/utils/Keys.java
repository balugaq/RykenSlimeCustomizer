package org.lins.mmmjjkx.rykenslimefuncustomizer.utils;

import org.bukkit.NamespacedKey;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

import java.util.Locale;

public class Keys {
    public static NamespacedKey newKey(String key) {
        return new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, key.toLowerCase(Locale.ROOT));
    }
}
