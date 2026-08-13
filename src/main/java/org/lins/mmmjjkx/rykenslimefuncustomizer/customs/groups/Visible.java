package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface Visible {
    boolean apply(Player p, PlayerProfile profile, SlimefunGuideMode layout);
}
