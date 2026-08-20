package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.Recipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.RecipeReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.menu.CustomMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@NullMarked
public interface TickerCreator extends RecipeReader {
    default List<? extends Recipe> readRecipes(SlimefunItem item) {
        if (item instanceof AdvancedCustomMachine acm) {
            return acm.getTicker().getRecipes();
        } else if (item instanceof AContainer ac) {
            return CustomMachineRecipe.from(ac.getMachineRecipes());
        } else {
            return new ArrayList<>();
        }
    }

    @Nullable
    MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon);
}
