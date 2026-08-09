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
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomMultiBlockMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiBlockMachineReader extends YamlReader<CustomMultiBlockMachine> {
    @Override
    public String getFileName() {
        return Constants.MULTI_BLOCK_MACHINES_FILE;
    }

    public MultiBlockMachineReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public CustomMultiBlockMachine readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        var base = getBase(section, s);
        if (base == null) return null;

        ConfigurationSection recipesSection = section.getConfigurationSection("recipes");

        int workSlot = section.getInt("work", -1);
        if (workSlot < 1 || workSlot > 9) {
            Debug.error(file, section, "缺少或配置错误 '工作槽' (slot)", 1, 9);
            return null;
        }

        if (base.recipe() == null) {
            Debug.error(file, section, "缺少或配置错误 '配方' (recipe)");
            return null;
        }

        boolean hasDispenser = false;

        for (ItemStack is : base.recipe()) {
            if (is != null) {
                if (is.getType() == Material.DISPENSER) {
                    hasDispenser = true;
                    break;
                }
            }
        }

        if (!hasDispenser) {
            Debug.error(file, section, "缺少发射器");
            return null;
        }

        if (base.recipe()[workSlot - 1] == null) {
            Debug.error(file, section, "工作槽对应的方块不存在");
            return null;
        }

        Map<ItemStack[], ItemStack> recipes = readRecipes(s, recipesSection, addon);
        SoundEffect sound = null;
        if (section.contains("sound")) {
            String soundString = section.getString("sound");
            Pair<ExceptionHandler.HandleResult, SoundEffect> soundEffectPair = ExceptionHandler.handleEnumValueOf(
                    "在附属" + addon.getAddonId() + "中加载多方块机器" + s + "无法获取声音类型" + soundString,
                    SoundEffect.class,
                    soundString);
            ExceptionHandler.HandleResult result1 = soundEffectPair.getFirstValue();
            if (result1 != ExceptionHandler.HandleResult.FAILED && soundEffectPair.getSecondValue() != null) {
                sound = soundEffectPair.getSecondValue();
            }
        }

        JavaScriptEval eval = getScriptOrNull(section, section.getString("script"));

        return new CustomMultiBlockMachine(base, recipes, workSlot, sound, eval);
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return anyPreloadItems(s);
    }

    public static Map<ItemStack[], ItemStack> getPreaddRecipes(String s) {
        return RykenSlimefunCustomizer.addonManager.getPreaddRecipes(s);
    }

    public static void addPreaddRecipe(String s, ItemStack[] input, ItemStack output) {
        RykenSlimefunCustomizer.addonManager.addPreaddRecipe(s, input, output);
    }

    private Map<ItemStack[], ItemStack> readRecipes(String s, ConfigurationSection section, ProjectAddon addon) {
        Map<ItemStack[], ItemStack> map = new HashMap<>();
        map.putAll(getPreaddRecipes(s));
        if (section == null) return map;

        for (String key : section.getKeys(false)) {
            ConfigurationSection recipe = section.getConfigurationSection(key);
            if (recipe == null) continue;
            ConfigurationSection inputs = recipe.getConfigurationSection("input");
            if (inputs == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "没有输入物品");
                continue;
            }
            ItemStack[] input = CommonUtils.readRecipe(inputs, addon);
            if (input == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "输入物品为空或格式错误");
                continue;
            }
            ConfigurationSection outputs = recipe.getConfigurationSection("output");
            if (outputs == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "没有输出物品");
                continue;
            }
            ItemStack output = CommonUtils.readItem(outputs, true, addon);
            if (output == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "输出物品为空或格式错误");
                continue;
            }
            map.put(input, output);
        }
        return map;
    }
}
