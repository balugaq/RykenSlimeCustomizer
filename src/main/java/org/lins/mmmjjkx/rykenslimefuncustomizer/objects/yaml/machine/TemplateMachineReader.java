/*
 * RykenSlimefunCustomizer
 * Copyright (C) 2026 lijinhong11(mmmjjjkx) and balugaq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.RecipeReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomTemplateMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.MachineTemplate;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TemplateMachineReader extends YamlReader<CustomTemplateMachine> {
    @Override
    public String getFileName() {
        return Constants.TEMPLATE_MACHINES_FILE;
    }

    public TemplateMachineReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public CustomTemplateMachine readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;

        String id = addon.getId(s, section.getString("id_alias"));
        ExceptionHandler.HandleResult result = ExceptionHandler.handleIdConflict(id);

        if (result == ExceptionHandler.HandleResult.FAILED) return null;

        String igId = section.getString("item_group");

        SlimefunItemStack sfis = getPreloadItem(id);
        if (sfis == null) return null;

        Pair<ExceptionHandler.HandleResult, ItemGroup> group = ExceptionHandler.handleItemGroupGet(addon, igId);
        if (group.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        Pair<RecipeType, ItemStack[]> recipePair = getRecipe(section, addon);
        RecipeType rt = recipePair.getFirstValue();
        ItemStack[] recipe = recipePair.getSecondValue();

        boolean fasterIfMoreTemplates = section.getBoolean("fasterIfMoreTemplates", false);
        boolean moreOutputIfMoreTemplates = section.getBoolean("moreOutputIfMoreTemplates", false);

        List<Integer> input = section.getIntegerList("input");
        List<Integer> output = section.getIntegerList("output");

        if (output.isEmpty()) {
            Debug.error(file, section, "缺少或配置错误 '输出槽' (output)");
            return null;
        }

        if (!input.isEmpty() && isInvalidSlots(input, section, ItemTransportFlow.INSERT)
            || isInvalidSlots(output, section, ItemTransportFlow.WITHDRAW)) {
            return null;
        }

        CustomMenu menu = CommonUtils.getIf(addon.getMenus(), m -> m.getId().equalsIgnoreCase(id));
        if (menu == null) {
            Debug.warning(file, section, "未找到菜单 " + id + " (menu), 使用默认菜单");
        }

        List<MachineTemplate> templates =
                readTemplates(id, input.size(), output.size(), section.getConfigurationSection("recipes"), addon);

        int templateSlot = section.getInt("templateSlot");
        if (templateSlot < 0 || templateSlot > 53) {
            Debug.error(file, section, "缺少或配置错误 '模板槽位' (templateSlot)", 0, 53);
            return null;
        }

        int capacity = section.getInt("capacity", -1);
        if (capacity <= 0) {
            Debug.error(file, section, "缺少或配置错误 '能源容量' (capacity)", 1, Integer.MAX_VALUE);
            return null;
        }

        int energy = section.getInt("consumption", -1);
        if (energy <= 0) {
            Debug.error(file, section, "缺少或配置错误 '能量消耗' (consumption)", 1, Integer.MAX_VALUE);
            return null;
        }

        boolean hideAllRecipes = section.getBoolean("hideAllRecipes", false);

        return new CustomTemplateMachine(
                group.getSecondValue(),
                sfis,
                rt,
                recipe,
                menu,
                input,
                output,
                templateSlot,
                templates,
                energy,
                capacity,
                fasterIfMoreTemplates,
                moreOutputIfMoreTemplates,
                hideAllRecipes);
    }

    private List<MachineTemplate> readTemplates(
            String s, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        List<MachineTemplate> list = new ArrayList<>();
        if (section == null) {
            return list;
        }

        for (String key : section.getKeys(false)) {
            SlimefunItemStack item = getPreloadItem(key);
            if (item == null) {
                SlimefunItem item2 = SlimefunItem.getById(key);
                if (item2 != null) {
                    item = ((SlimefunItemStack) item2.getItem().clone());
                }
            }

            if (item == null) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载模板机器" + s + "时遇到了问题: 无法找到作为模板的物品" + key);
                continue;
            }

            List<CustomMachineRecipe> recipes =
                    readRecipes(s, inputSize, outputSize, section.getConfigurationSection(key), addon);
            list.add(new MachineTemplate(item, recipes));
        }

        return list;
    }

    private List<CustomMachineRecipe> readRecipes(
            String s, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        List<CustomMachineRecipe> list = new ArrayList<>();
        if (section == null) {
            return list;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection recipes = section.getConfigurationSection(key);
            if (recipes == null) continue;
            int seconds = recipes.getInt("seconds");
            if (seconds < 0) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载模板机器" + s + "的工作配方" + key + "时遇到了问题: " + "间隔时间未设置或不能小于0");
                continue;
            }

            ConfigurationSection inputs = recipes.getConfigurationSection("input");
            ItemStack[] input = CommonUtils.readRecipe(inputs, addon, inputSize);
            ConfigurationSection outputs = recipes.getConfigurationSection("output");
            if (outputs == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载模板机器" + s + "的工作配方" + key + "时遇到了问题: " + "没有输出物品");
                continue;
            }

            IntList chances = new IntArrayList();

            ItemStack[] output = new ItemStack[outputSize];
            for (int i = 0; i < outputSize; i++) {
                ConfigurationSection section1 = outputs.getConfigurationSection(String.valueOf(i + 1));
                var item = CommonUtils.readItem(section1, true, addon);
                if (item != null) {
                    int chance = section1.getInt("chance", 100);

                    if (chance < 1) {
                        ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载模板机器" + s + "的工作配方" + key
                                + "时遇到了问题: " + "概率不应该小于1，已转为1");
                        chance = 1;
                    }

                    output[i] = item;
                    chances.add(chance);
                }
            }

            RecipeReader.addToList(list, recipes, seconds, input, chances, output);
        }
        return list;
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return blockPreloadItems(s);
    }
}
