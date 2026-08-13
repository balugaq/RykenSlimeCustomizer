package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.AbstractRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.menu.CustomMenu;

import java.io.File;
import java.util.List;

@NullMarked
public class WorkbenchMachineTickerCreator extends LinkedRecipeMachineTickerCreator {
    @Override
    public @Nullable MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon) {
        var recipes = read(file, sf.getInputSlots().length, sf.getOutputSlots().length, section, addon);
        if (recipes == null) return null;
        return new WorkBenchMachineTicker() {
            @Override
            public int getEnergyConsumption() {
                return sf.getEnergyConsumption();
            }

            @Override
            public int getCapacity() {
                return sf.getCapacity();
            }

            @Override
            public @Nullable CustomMenu getCustomMenu() {
                return menu;
            }

            @Override
            public AdvancedCustomMachine getMachine() {
                return sf;
            }

            @Override
            public int[] getInputSlots() {
                return sf.getInputSlots();
            }

            @Override
            public int[] getOutputSlots() {
                return sf.getOutputSlots();
            }

            @Override
            public List<? extends AbstractRecipe> getRecipes() {
                return recipes;
            }
        };
    }

}
