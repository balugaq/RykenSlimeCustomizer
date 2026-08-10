package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.LinkedOutput;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomLinkedMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NullMarked
public class LinkedRecipeMachineTickerCreator extends RecipeMachineTickerCreator {
    @Override
    public @Nullable List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        var recipes = section.getConfigurationSection("recipes");
        if (recipes == null) return Collections.emptyList();

        int saveAmount = section.getInt("saveAmount", 0);
        if (saveAmount < 0 || saveAmount >= 63) {
            Debug.error(file, recipes, "配置错误 '预留数量' (saveAmount)", 0, 62);
            return null;
        }

        List<CustomLinkedMachineRecipe> list = new ArrayList<>();
        for (String key : recipes.getKeys(false)) {
            ConfigurationSection recipe = recipes.getConfigurationSection(key);
            if (recipe == null) continue;
            int seconds = recipe.getInt("seconds");
            if (seconds < 0) {
                Debug.warning(file, recipe, "缺少或配置错误 '时间间隔' (seconds)");
                continue;
            }
            ConfigurationSection inputs = recipe.getConfigurationSection("input");
            if (inputs == null) {
                Debug.warning(file, recipe, "缺少 '输入物品' (input)");
                continue;
            }

            ConfigurationSection outputs = recipe.getConfigurationSection("output");
            if (outputs == null) {
                Debug.warning(file, recipe, "缺少 '输出物品' (output)");
                continue;
            }

            List<ItemStack> freeOutput = new ArrayList<>();
            List<Integer> freeChances = new ArrayList<>();

            Map<Integer, ItemStack> linkedOutput = new HashMap<>();
            Map<Integer, Integer> linkedChances = new HashMap<>();

            for (int i = 0; i < outputSize; i++) {
                ConfigurationSection section1 = outputs.getConfigurationSection(String.valueOf(i + 1));
                var item = CommonUtils.readItem(file, section1, addon);
                if (item != null) {
                    int chance = CommonUtils.clamp(section1.getInt("chance", 100), 1, 100,
                        file, section1, "'概率 (chance) 非法'");

                    int slot = section1.getInt("slot", -1);
                    if (slot == -1) {
                        freeOutput.add(item);
                        freeChances.add(chance);
                    } else {
                        linkedOutput.put(slot, item);
                        linkedChances.put(slot, chance);
                    }
                }
            }

            boolean chooseOne = recipe.getBoolean("chooseOne", false);
            boolean forDisplay = recipe.getBoolean("forDisplay", false);
            boolean hide = recipe.getBoolean("hide", false);
            boolean noConsume = recipe.getBoolean("noConsume", false);

            Set<Integer> noConsumes = new HashSet<>();
            Map<Integer, ItemStack> finalInput = new HashMap<>();
            for (int i = 0; i < inputSize; i++) {
                ConfigurationSection section1 = inputs.getConfigurationSection(String.valueOf(i + 1));
                if (section1 == null) {
                    continue;
                }

                ItemStack itemStack = CommonUtils.readItem(file, section1, addon);
                if (itemStack == null) {
                    continue;
                }

                int slot = section1.getInt("slot", -1);
                if (slot == -1) {
                    Debug.warning(file, recipe, "缺少或配置错误 '槽位' (slot)");
                    continue;
                }

                if (slot < 0 || slot > 53) {
                    Debug.warning(file, recipe, "'槽位' 非法 (slot)");
                    continue;
                }

                finalInput.put(slot, itemStack);

                boolean noConsume1 = section1.getBoolean("noConsume", false);
                if (noConsume1) {
                    noConsumes.add(slot);
                }
            }

            int[] array = new int[freeChances.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = i;
            }

            list.add(new CustomLinkedMachineRecipe(
                seconds,
                finalInput,
                new LinkedOutput(freeOutput.toArray(new ItemStack[0]), InvIndex.mergeItems(freeOutput), linkedOutput, array, linkedChances),
                chooseOne,
                forDisplay,
                hide,
                noConsumes,
                saveAmount));
        }
        return list;
    }

    @Override
    public @Nullable MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @org.jspecify.annotations.Nullable CustomMenu menu, ProjectAddon addon) {
        var recipes = read(file, sf.getInputSlots().length, sf.getOutputSlots().length, section, addon);
        if (recipes == null) return null;
        return new LinkedRecipeMachineTicker() {
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
