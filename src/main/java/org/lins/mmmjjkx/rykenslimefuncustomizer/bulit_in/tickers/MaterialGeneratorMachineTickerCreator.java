package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.AbstractRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.menu.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@NullMarked
public class MaterialGeneratorMachineTickerCreator implements TickerCreator {
    @Override
    public @Nullable List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        ConfigurationSection outputItems = section.getConfigurationSection("outputs");
        List<ItemStack> outputs = new ArrayList<>();
        IntList chances = new IntArrayList();
        boolean chooseOne = section.getBoolean("chooseOne", false);

        if (outputItems != null) {
            for (String key : outputItems.getKeys(false)) {
                ConfigurationSection outputCfg = outputItems.getConfigurationSection(key);
                if (outputCfg == null) break;
                var item = CommonUtils.readItem(file, outputCfg, addon);
                if (item == null) {
                    Debug.error(file, outputCfg, "物品配置错误 (outputs)");
                    continue;
                }

                int chance = CommonUtils.clamp(outputCfg.getInt("chance", 100), 1, 100,
                    file, outputCfg, "'概率 (chance) 非法'");

                outputs.add(item);
                chances.add(chance);
            }
        }

        ConfigurationSection outputItem = section.getConfigurationSection("outputItem");
        if (outputItem != null) {
            var item = CommonUtils.readItem(file, outputItem, addon);
            if (item == null) {
                Debug.error(file, outputItem, "物品配置错误 (outputItem)");
            } else {

                int chance = CommonUtils.clamp(outputItem.getInt("chance", 100), 1, 100,
                    file, outputItem, "'概率 (chance) 非法'");

                outputs.add(item);
                chances.add(chance);
            }
        }

        int tickRate = section.getInt("tickRate");
        if (tickRate < 1) {
            Debug.error(file, section, "配置错误 '配方耗时' (tickRate)", 1, Integer.MAX_VALUE);
            return null;
        }
        return List.of(new CustomMachineRecipe(
            List.of(),
            outputs.stream().toList().toArray(new ItemStack[0]),
            tickRate,
            chances,
            chooseOne,
            false,
            false
        ));
    }

    @Override
    public @Nullable MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon) {
        var recipes = read(file, sf.getInputSlots().length, sf.getOutputSlots().length, section, addon);
        if (recipes == null) return null;
        int status = section.getInt("status", -1);
        if (status < -1) {
            Debug.error(file, section, "缺少或配置错误 '状态槽' (status)", -1, 53);
            return null;
        }

        return new MaterialGeneartorMachineTicker() {
            @Override
            public @Range(from = -1, to = 53) int getStatusSlot() {
                return status;
            }

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
