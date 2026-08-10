package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.AdvancedCustomMachine;

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
            public SlimefunItem getSlimefunItem() {
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
