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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomRecipeMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomSuperMultiBlockMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.CustomMultiBlockPart;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.MultiBlockPart;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SlimefunMultiBlockPart;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockDefinition;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.VanillaMultiBlockPart;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.Vector3i;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class SuperMultiBlockMachineReader extends YamlReader<CustomSuperMultiBlockMachine> {
    public SuperMultiBlockMachineReader(YamlConfiguration config, ProjectAddon addon) {
        super(config, addon);
    }

    @Override
    public CustomSuperMultiBlockMachine readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        String id = addon.getId(s, section.getString("id_alias"));

        ExceptionHandler.HandleResult result = ExceptionHandler.handleIdConflict(id);

        if (result == ExceptionHandler.HandleResult.FAILED) return null;

        String igId = section.getString("item_group");
        Pair<ExceptionHandler.HandleResult, ItemGroup> group = ExceptionHandler.handleItemGroupGet(addon, igId);
        if (group.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        SlimefunItemStack slimefunItemStack = getPreloadItem(id);
        if (slimefunItemStack == null) return null;
        if (!slimefunItemStack.getType().isBlock()) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "物品类型不是方块");
            return null;
        }

        Pair<RecipeType, ItemStack[]> recipePair = getRecipe(section, addon);
        RecipeType rt = recipePair.getFirstValue();
        ItemStack[] recipe = recipePair.getSecondValue();

        CustomMenu menu = CommonUtils.getIf(addon.getMenus(), m -> m.getID().equalsIgnoreCase(id));
        if (menu == null) {
            ExceptionHandler.handleWarning("未找到菜单 " + id + " 使用默认菜单");
        }

        List<Integer> input = section.getIntegerList("input");
        List<Integer> output = section.getIntegerList("output");

        if (input.isEmpty()) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "输入槽为空");
            return null;
        }

        if (output.isEmpty()) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "输出槽为空");
            return null;
        }

        ConfigurationSection recipes = section.getConfigurationSection("recipes");

        int capacity = section.getInt("capacity");

        if (capacity < 0) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "能源容量小于0");
            return null;
        }

        int energy = section.getInt("energyPerCraft");

        if (energy <= 0) {
            ExceptionHandler.handleError(
                    "在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "合成一次的消耗能量未设置或小于等于0");
            return null;
        }

        int speed = section.getInt("speed");

        if (speed <= 0) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "合成速度未设置或小于等于0");
            return null;
        }

        boolean hideAllRecipes = section.getBoolean("hideAllRecipes", false);

        List<CustomMachineRecipe> mr = readRecipes(s, input.size(), output.size(), recipes, addon);

        JavaScriptEval eval = null;
        if (section.contains("script")) {
            String script = section.getString("script", "");
            File file = new File(addon.getScriptsFolder(), script + ".js");
            if (!file.exists()) {
                ExceptionHandler.handleWarning(
                        "在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "找不到脚本文件 " + file.getName());
            } else {
                eval = JavaScriptEval.create(file, addon);
            }
        }

        boolean displayProjectiles = section.getBoolean("displayProjectiles", true);

        SuperMultiBlockDefinition definition = readMultiBlockDefinition(section, s, eval);
        if (definition == null) return null;

        return new CustomSuperMultiBlockMachine(
                group.getSecondValue(),
                slimefunItemStack,
                rt,
                recipe,
                input.stream().mapToInt(x -> x).toArray(),
                output.stream().mapToInt(x -> x).toArray(),
                mr,
                energy,
                capacity,
                menu,
                speed,
                hideAllRecipes,
                eval,
                definition,
                displayProjectiles
        );
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;

        ConfigurationSection item = section.getConfigurationSection("item");
        ItemStack stack = CommonUtils.readItem(item, false, addon);

        if (stack == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "物品为空或格式错误导致无法加载");
            return null;
        }

        return List.of(new SlimefunItemStack(addon.getId(s, section.getString("id_alias")), stack));
    }

    public static Map<ItemStack[], ItemStack> getPreaddRecipes(String s) {
        return RykenSlimefunCustomizer.addonManager.getPreaddRecipes(s);
    }

    public static void addPreaddRecipe(String s, ItemStack[] input, ItemStack output) {
        RykenSlimefunCustomizer.addonManager.addPreaddRecipe(s, input, output);
    }

    @Nullable
    private SuperMultiBlockDefinition readMultiBlockDefinition(ConfigurationSection section, String s, @Nullable JavaScriptEval eval) {
        if (section == null) return null;
        if (!section.contains("structure")) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "没有结构定义");
            return null;
        }

        // structure是一个List<List<String>>，方块用一个字符串来表示，一个字符串里包含若干个字符串
        // 每个字符串空格分隔， 其中只以 "_" 构成的字符串，特殊，表示无方块，
        // List<String>表示一个平面结构
        // 和 structure 同级，有 mapping.字符串，表示每个字符串对应的方块描述
        // 例如：
        // structure:
        // - 
        //  - "__ b1 __"
        //  - "a1 b1 c1"
        //  - "__ b1 __"
        // -
        //  - "c1 b1 a1"
        //  - "c1 o  a1"
        //  - "c1 b1 a1"
        // -
        //  - "b1 a1 c1"
        //  - "b1 a1 c1"
        //  - "b1 a1 c1"
        // 考虑到多方块结构不一定是规则图形
        // 需要先确定 o 即core的位置，然后对其他进行向量化 -> Vector3i
        // 在 o 上面的是 y+1, o下面的是y-1
        // o 所在的index对应到其他layer上的位置，就是y轴所在的位置

        Pair<Map<String, MultiBlockPart>, String> mappingAndCore = readMapping(section.getConfigurationSection("mapping"), s, eval);
        if (mappingAndCore == null) return null;
        Map<String, MultiBlockPart> mapping = mappingAndCore.getFirstValue();
        String core = mappingAndCore.getSecondValue();

        List<?> structure0 = section.getList("structure");
        if (structure0 == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "结构定义为空");
            return null;
        }
        List<List<String>> structure = structure0.stream().map(x -> (List<String>) x).collect(Collectors.toList());
        if (structure.isEmpty()) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "结构定义为空");
            return null;
        }

        Vector3i corePos = null;
        Map<Vector3i, String> blockPositions = new HashMap<>();
        for (int i = 0; i < structure.size(); i++) {
            List<String> layer = structure.get(i);
            for (int j = 0; j < layer.size(); j++) {
                String line = layer.get(j);
                String[] blocks = line.split(" ");
                for (int k = 0; k < blocks.length; k++) {
                    String block = blocks[k];
                    if (isVaildBlockDesc(block)) {
                        blockPositions.put(new Vector3i(j, -i, k), block);
                    }
                    if (block.equals(core)) {
                        if (corePos != null) {
                            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "在 structure 中存在多个 core，无法判定core");
                            return null;
                        }

                        corePos = new Vector3i(j, -i, k);
                    }
                }
            }
        }
        
        if (corePos == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "在 structure 中不存在 core");
            return null;
        }

        MultiBlockPart corePart = mapping.get(core);
        if (corePart == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "在 structure.mapping 中不存在 core");
            return null;
        }
        Map<Vector3i, MultiBlockPart> blockParts = new HashMap<>();
        for (Vector3i pos : blockPositions.keySet()) {
            String blockDesc = blockPositions.get(pos);
            if (!mapping.containsKey(blockDesc)) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "在 structure 中存在不存在的 blockDesc: " + blockDesc);
                return null;
            }
            blockParts.put(pos.subtract(corePos), mapping.get(blockDesc));
        }

        SuperMultiBlockDefinition definition = new SuperMultiBlockDefinition(corePart, blockParts);
        return definition;
    }

    public static boolean isVaildBlockDesc(String block) {
        for (char c : block.toCharArray()) {
            if (c != '_') return false;
        }
        return true;
    }

    @Nullable
    private Pair<Map<String, MultiBlockPart>, String> readMapping(ConfigurationSection section, String s, @Nullable JavaScriptEval eval) {
        if (section == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "没有 structure.mapping 定义");
            return null;
        }
        Map<String, MultiBlockPart> mapping = new HashMap<>();
        String core = null;
        for (String key : section.getKeys(false)) {
            var partSection = section.getConfigurationSection(key);
            if (partSection.contains("core")) {
                if (core != null) {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "在 structure.mapping 中存在多个 core，无法判定core");
                    return null;
                }
                core = key;
            }
            mapping.put(key, readMultiBlockPart(section, s, eval, key));
        }

        if (core == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + "在 structure.mapping 中不存在 core");
            return null;
        }

        return new Pair<Map<String,MultiBlockPart>,String>(mapping, core);
    }

    @Nullable
    private MultiBlockPart readMultiBlockPart(ConfigurationSection section, String s, @Nullable JavaScriptEval eval, String mappingLocation) {
        // 读取多方块结构定义
        // material_type: mc / slimefun / custom
        // material: 方块/BlockData/粘液id
        // 对于 custom，由脚本代理检查
        String type = section.getString("material_type", "");
        String material = section.getString("material", "");
        if (type.isEmpty() || material.isEmpty()) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + mappingLocation + " 类型或材料为空");
            return null;
        }

        if (type.equals("mc")) {
            Material m = Material.matchMaterial(material);
            // 当存在 material 且有效时，data 不会被使用
            String data = section.getString("data");
            BlockData blockData = data == null ? null : Bukkit.createBlockData(data);
            if (blockData == null) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + mappingLocation + " 材料 " + material + " 无效");
                return null;
            }
            return new VanillaMultiBlockPart(m, blockData);
        }

        if (type.equals("slimefun")) {
            SlimefunItem slimefunItem = SlimefunItem.getById(material.toUpperCase());
            if (slimefunItem == null || !slimefunItem.getItem().getType().isBlock()) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + mappingLocation + " 材料 " + material + " 无效");
                return null;
            }
            return new SlimefunMultiBlockPart(slimefunItem);
        }

        if (type.equals("custom")) {
            BlockData blockData = null;
            
            if (material != null) {
                blockData = Bukkit.createBlockData(material);
                if (blockData == null) {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + mappingLocation + " 材料 " + material + " 无效");
                    return null;
                }
            }
            return new CustomMultiBlockPart(eval, blockData);
        }

        ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "时遇到了问题: " + mappingLocation + " 无效的类型: " + type);
        return null;
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
                        "在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "间隔时间未设置或不能小于0");
                continue;
            }
            ConfigurationSection inputs = recipes.getConfigurationSection("input");
            if (inputs == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "没有输入物品");
                continue;
            }
            ItemStack[] input = CommonUtils.readRecipe(inputs, addon, inputSize);
            if (input == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "输入物品为空或格式错误");
                continue;
            }
            ConfigurationSection outputs = recipes.getConfigurationSection("output");
            if (outputs == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "的工作配方" + key + "时遇到了问题: " + "没有输出物品");
                continue;
            }

            List<Integer> chances = new ArrayList<>();

            ItemStack[] output = new ItemStack[outputSize];
            for (int i = 0; i < outputSize; i++) {
                ConfigurationSection section1 = outputs.getConfigurationSection(String.valueOf(i + 1));
                var item = CommonUtils.readItem(section1, true, addon);
                if (item != null) {
                    int chance = section1.getInt("chance", 100);

                    if (chance < 1) {
                        ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载超级多方块机器" + s + "的工作配方" + key
                                + "时遇到了问题: " + "概率不应该小于1，已转为1");
                        chance = 1;
                    }

                    output[i] = item;
                    chances.add(chance);
                }
            }

            RecipeMachineReader.addToList(list, recipes, seconds, input, chances, output);
        }
        return list;
    }
}