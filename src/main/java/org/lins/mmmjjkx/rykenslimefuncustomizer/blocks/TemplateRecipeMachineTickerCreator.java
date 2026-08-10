package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomTemplateMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NullMarked
public class TemplateRecipeMachineTickerCreator extends RecipeMachineTickerCreator {
    @Override
    public @Nullable List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        int templateSlot = section.getInt("templateSlot"); // checked
        boolean moreOutputIfMoreTemplates = section.getBoolean("moreOutputIfMoreTemplates", false);

        List<CustomTemplateMachineRecipe> result = new ArrayList<>();
        var recipes = section.getConfigurationSection("recipes");
        if (recipes == null) return Collections.emptyList();

        for (String key : recipes.getKeys(false)) {
            SlimefunItemStack item = addon.getPreloadItems().get(key);
            if (item == null) {
                SlimefunItem item2 = SlimefunItem.getById(key);
                if (item2 != null) {
                    item = ((SlimefunItemStack) item2.getItem().clone());
                }
            }

            if (item == null) {
                Debug.error(file, section, "无法找到作为模板的物品: " + key);
                continue;
            }

            var r = readRecipes(file, inputSize, outputSize, section, section.getConfigurationSection("recipes"), addon);
            if (r != null) {
                for (var recipe : r) {
                    result.add(new CustomTemplateMachineRecipe(templateSlot, item, recipe, moreOutputIfMoreTemplates));
                }
            }
        }
        return result;
    }

    @Override
    public @Nullable MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon) {
        int templateSlot = section.getInt("templateSlot");
        if (templateSlot < 0 || templateSlot > 53) {
            Debug.error(file, section, "缺少或配置错误 '模板槽位' (templateSlot)", 0, 53);
            return null;
        }

        var recipes = read(file, sf.getInputSlots().length, sf.getOutputSlots().length, section, addon);
        if (recipes == null) return null;
        boolean fasterIfMoreTemplates = section.getBoolean("fasterIfMoreTemplates", false);
        return new TemplateRecipeMachineTicker() {
            @Override
            public int getTemplateSlot() {
                return templateSlot;
            }

            @Override
            public boolean isFasterIfMoreTemplates() {
                return fasterIfMoreTemplates;
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

            @Override
            public SlimefunItem getSlimefunItem() {
                return sf;
            }
        };
    }
}
