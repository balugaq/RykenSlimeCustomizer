package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.RecipeReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.menu.CustomMenu;

import java.io.File;

public interface TickerCreator extends RecipeReader {
    @Nullable
    MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon);
}
