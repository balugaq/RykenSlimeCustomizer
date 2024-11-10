package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomLinkedRecipeMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomLinkedMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * RSC_EXAMPLE_LINKED_RECIPE_MACHINE:
 *   item_group: RSC_EXAMPLE_NORMAL_GROUP
 *   item:
 *     material: EMERALD_BLOCK
 *   inputs: [19, 20]
 *   outputs: [24, 25]
 *   recipes:
 *     your_recipe_name:
 *       seconds: 10
 *       input:
 *         1:
 *           slot: 19
 *           material: IRON_INGOT
 *           amount: 64
 *         2:
 *           slot: 20
 *           material: IRON_INGOT
 *           amount: 64
 *       output:
 *         1:
 *           material: GOLD_INGOT
 *
 */
public class LinkedRecipeMachineReader extends YamlReader<CustomLinkedRecipeMachine> {
    public LinkedRecipeMachineReader(YamlConfiguration config, ProjectAddon addon) {
        super(config, addon);
    }

    @Override
    public CustomLinkedRecipeMachine readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        String id = section.getString("id_alias", s).toUpperCase();

        ExceptionHandler.HandleResult result = ExceptionHandler.handleIdConflict(id);

        if (result == ExceptionHandler.HandleResult.FAILED) return null;

        String igId = section.getString("item_group");
        Pair<ExceptionHandler.HandleResult, ItemGroup> group = ExceptionHandler.handleItemGroupGet(addon, igId);
        if (group.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        SlimefunItemStack slimefunItemStack = getPreloadItem(id);
        if (slimefunItemStack == null) return null;

        ItemStack[] recipe = CommonUtils.readRecipe(section.getConfigurationSection("recipe"), addon);
        String recipeType = section.getString("recipe_type", "NULL");

        Pair<ExceptionHandler.HandleResult, RecipeType> rt = ExceptionHandler.getRecipeType(
                "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "时遇到了问题: " + "错误的配方类型" + recipeType + "!", recipeType);

        if (rt.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        CustomMenu menu = CommonUtils.getIf(addon.getMenus(), m -> m.getID().equalsIgnoreCase(id));
        if (menu == null) {
            ExceptionHandler.handleWarning("未找到菜单 " + id + " 使用默认菜单");
        }

        List<Integer> input = section.getIntegerList("input");
        List<Integer> output = section.getIntegerList("output");

        if (input.isEmpty()) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载强配方机器" + s + "时遇到了问题: " + "输入槽为空");
            return null;
        }

        if (output.isEmpty()) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载强配方机器" + s + "时遇到了问题: " + "输出槽为空");
            return null;
        }

        ConfigurationSection recipes = section.getConfigurationSection("recipes");

        int capacity = section.getInt("capacity");

        if (capacity < 0) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载强配方机器" + s + "时遇到了问题: " + "能源容量小于0");
            return null;
        }

        int energy = section.getInt("energyPerCraft");

        if (energy <= 0) {
            ExceptionHandler.handleError(
                    "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "时遇到了问题: " + "合成一次的消耗能量未设置或小于等于0");
            return null;
        }

        int speed = section.getInt("speed");

        if (speed <= 0) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载强配方机器" + s + "时遇到了问题: " + "合成速度未设置或小于等于0");
            return null;
        }

        boolean hideAllRecipes = section.getBoolean("hideAllRecipes", false);

        List<CustomLinkedMachineRecipe> mr = readRecipes(s, input.size(), output.size(), recipes, addon);

        return new CustomLinkedRecipeMachine(
                group.getSecondValue(),
                slimefunItemStack,
                rt.getSecondValue(),
                recipe,
                input.stream().mapToInt(x -> x).toArray(),
                output.stream().mapToInt(x -> x).toArray(),
                mr,
                energy,
                capacity,
                menu,
                speed,
                hideAllRecipes);
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;

        ConfigurationSection item = section.getConfigurationSection("item");
        ItemStack stack = CommonUtils.readItem(item, false, addon);

        if (stack == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载强配方机器" + s + "时遇到了问题: " + "物品为空或格式错误");
            return null;
        }

        return List.of(new SlimefunItemStack(section.getString("id_alias", s).toUpperCase(), stack));
    }

    private List<CustomLinkedMachineRecipe> readRecipes(
            String s, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        List<CustomLinkedMachineRecipe> list = new ArrayList<>();
        if (section == null) {
            return list;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection recipes = section.getConfigurationSection(key);
            if (recipes == null) continue;
            int seconds = recipes.getInt("seconds");
            if (seconds < 0) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key + "时遇到了问题: " + "间隔时间未设置或不能小于0");
                continue;
            }
            ConfigurationSection inputs = recipes.getConfigurationSection("input");
            if (inputs == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key + "时遇到了问题: " + "没有输入物品");
                continue;
            }
            ItemStack[] input = CommonUtils.readRecipe(inputs, addon, inputSize);
            if (input == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key + "时遇到了问题: " + "输入物品为空或格式错误");
                continue;
            }
            int[] slots = CommonUtils.readSlot(inputs, addon, inputSize);
            if (slots == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key + "时遇到了问题: " + "输入槽位为空或格式错误");
                continue;
            }
            for (int sss : slots) {
                if (sss == -1) {
                    ExceptionHandler.handleError(
                            "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key + "时遇到了问题: " + "输入槽位不能为空");
                    continue;
                }

                if (sss < 0 || sss > 53) {
                    ExceptionHandler.handleError(
                            "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key + "时遇到了问题: " + "输入槽位超出范围");
                    continue;
                }
            }
            ConfigurationSection outputs = recipes.getConfigurationSection("output");
            if (outputs == null) {
                ExceptionHandler.handleError(
                        "在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key + "时遇到了问题: " + "没有输出物品");
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
                        ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载强配方机器" + s + "的工作配方" + key
                                + "时遇到了问题: " + "概率不应该小于1，已转为1");
                        chance = 1;
                    }

                    output[i] = item;
                    chances.add(chance);
                }
            }

            boolean chooseOne = recipes.getBoolean("chooseOne", false);
            boolean forDisplay = recipes.getBoolean("forDisplay", false);
            boolean hide = recipes.getBoolean("hide", false);

            ItemStack[] input1 = new ItemStack[input.length];
            int[] slots1 = new int[slots.length];
            int index = 0;
            for (int i = 0; i < input.length; i++) {
                if (input[i] == null) {
                    continue;
                }
                input1[index] = input[i];
                slots1[index] = slots[i];
                index++;
            }
            output = CommonUtils.removeNulls(output);

            Map<Integer, ItemStack> finalInput = new HashMap<>();
            for (int i = 0; i < input1.length; i++) {
                finalInput.put(slots1[i], input1[i]);
            }

            list.add(new CustomLinkedMachineRecipe(seconds, finalInput, output, chances, chooseOne, forDisplay, hide));
        }
        return list;
    }
}
