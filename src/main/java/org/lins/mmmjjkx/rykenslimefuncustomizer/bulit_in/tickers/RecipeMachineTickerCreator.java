package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.AbstractRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.RecipeReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.InputWrapper;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.menu.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NullMarked
public class RecipeMachineTickerCreator implements TickerCreator {
    @Override
    public @Nullable List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        return readRecipes(file, section.getConfigurationSection("recipes"), addon, false);
    }

    public @Nullable List<CustomMachineRecipe> readRecipes(File file, @Nullable ConfigurationSection recipes, ProjectAddon addon, boolean canInputEmpty) {
        if (recipes == null) return Collections.emptyList();
        List<CustomMachineRecipe> list = new ArrayList<>();
        for (String key : recipes.getKeys(false)) {
            ConfigurationSection recipe = recipes.getConfigurationSection(key);
            if (recipe == null) continue;
            int seconds = recipe.getInt("seconds", -1);
            if (seconds < 0) {
                Debug.error(file, recipe, "缺少或配置错误 '配方耗时' (seconds)");
                continue;
            }
            List<InputWrapper> input = CommonUtils.readInputs(file, recipe.getConfigurationSection("input"), addon, recipe.getBoolean("noConsume", false));
            if (!canInputEmpty && input.isEmpty()) {
                Debug.error(file, recipe, "缺少 '输入物品' (input)");
                continue;
            }
            ConfigurationSection outputs = recipe.getConfigurationSection("output");
            if (outputs == null) {
                Debug.error(file, recipe, "缺少 '输出物品' (output)");
                continue;
            }

            List<ItemStack> output = new ArrayList<>();
            IntList chances = new IntArrayList();
            for (String k : outputs.getKeys(false)) {
                ConfigurationSection outputCfg = outputs.getConfigurationSection(k);
                if (outputCfg == null) break;
                var item = CommonUtils.readItem(file, outputCfg, addon);
                if (item == null) {
                    Debug.error(file, outputCfg, "物品配置错误 (output)");
                    continue;
                }

                int chance = CommonUtils.clamp(outputCfg.getInt("chance", 100), 1, 100,
                    file, outputCfg, "'概率 (chance) 非法'");

                output.add(item);
                chances.add(chance);
            }

            RecipeReader.addToList(list, recipe, seconds, input, chances, output.toArray(new ItemStack[0]));
        }
        return list;
    }

    @Override
    public @Nullable MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon) {
        var recipes = read(file, sf.getInputSlots().length, sf.getOutputSlots().length, section, addon);
        if (recipes == null) return null;
        if (recipes.isEmpty()) {
            Debug.warn("机器 " + sf.getId() + " 不含任何工作配方!");
        }
        return new RecipeMachineTicker() {
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
