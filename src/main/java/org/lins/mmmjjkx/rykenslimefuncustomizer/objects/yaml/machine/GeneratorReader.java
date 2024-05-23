package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotCardinallyRotatable;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotDiagonallyRotatable;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineFuel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomGenerator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.Rotation;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ClassUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class GeneratorReader extends YamlReader<CustomGenerator> {
    public GeneratorReader(YamlConfiguration config, ProjectAddon addon) {
        super(config, addon);
    }

    @SneakyThrows
    @Override
    public CustomGenerator readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        String id = section.getString(s + ".id_alias", s);

        ExceptionHandler.HandleResult result = ExceptionHandler.handleIdConflict(id);

        if (result == ExceptionHandler.HandleResult.FAILED) return null;

        String igId = section.getString("item_group");

        SlimefunItemStack sfis = getPreloadItem(id);
        if (sfis == null) return null;

        Pair<ExceptionHandler.HandleResult, ItemGroup> group = ExceptionHandler.handleItemGroupGet(addon, igId);
        if (group.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;
        ItemStack[] recipe = CommonUtils.readRecipe(section.getConfigurationSection("recipe"), addon);
        String recipeType = section.getString("recipe_type", "NULL");

        Pair<ExceptionHandler.HandleResult, RecipeType> rt =
                ExceptionHandler.getRecipeType("在附属" + addon.getAddonId() + "中加载发电机" + s + "时遇到了问题: " + "错误的配方类型" + recipeType + "!", recipeType);

        if (rt.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        CustomMenu menu = CommonUtils.getIf(addon.getMenus(), m -> m.getID().equalsIgnoreCase(s));

        List<Integer> input = section.getIntegerList("input");
        List<Integer> output = section.getIntegerList("output");

        ConfigurationSection fuelsSection = section.getConfigurationSection("fuels");
        List<MachineFuel> fuels = readFuels(s, fuelsSection, addon);
        int capacity = section.getInt("capacity", 0);
        int production = section.getInt("production");

        if (production < 1) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载发电机" + s + "时遇到了问题: " + "产电量不能小于1");
            return null;
        }

        Object[] constructorArgs = {
            group.getSecondValue(), sfis, rt.getSecondValue(), recipe, menu, capacity, input, output, production, fuels
        };

        String rotationStr = section.getString("rotation", "NOT_ROTATABLE");
        Pair<ExceptionHandler.HandleResult, Rotation> rotationPair =
                ExceptionHandler.handleEnumValueOf("在附属" + addon.getAddonId() + "中加载发电机" + s + "时遇到了问题: " + "错误的旋转类型: " + rotationStr + "!", Rotation.class, rotationStr);

        if (rotationPair.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        return switch (Objects.requireNonNull(rotationPair.getSecondValue())) {
            case NOT_ROTATABLE -> new CustomGenerator(
                    group.getSecondValue(),
                    sfis,
                    rt.getSecondValue(),
                    recipe,
                    menu,
                    capacity,
                    input,
                    output,
                    production,
                    fuels);
            case NOT_DIAGONALLY -> {
                Class<? extends CustomGenerator> clazz = (Class<? extends CustomGenerator>) ClassUtils.generateClass(
                        CustomGenerator.class,
                        "NotRiagonallyRotatable",
                        "Generator",
                        new Class[] {NotDiagonallyRotatable.class},
                        null);
                yield (CustomGenerator) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
            }
            case NOT_CARDINALLY -> {
                Class<? extends CustomGenerator> clazz = (Class<? extends CustomGenerator>) ClassUtils.generateClass(
                        CustomGenerator.class,
                        "NotCardinallyRotatable",
                        "Generator",
                        new Class[] {NotCardinallyRotatable.class},
                        null);
                yield (CustomGenerator) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
            }
        };
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        String id = section.getString(s + ".id_alias", s);

        ConfigurationSection item = section.getConfigurationSection("item");
        ItemStack stack = CommonUtils.readItem(item, false, addon);

        if (stack == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载发电机" + s + "时遇到了问题: " + "物品为空或格式错误导致无法加载");
            return null;
        }
        return List.of(new SlimefunItemStack(id, stack));
    }

    private List<MachineFuel> readFuels(String s, ConfigurationSection section, ProjectAddon addon) {
        List<MachineFuel> fuels = new ArrayList<>();

        if (section == null) return fuels;

        for (String key : section.getKeys(false)) {
            ConfigurationSection section1 = section.getConfigurationSection(key);
            if (section1 == null) continue;
            ConfigurationSection item = section1.getConfigurationSection("item");
            ItemStack stack = CommonUtils.readItem(item, true, addon);
            if (stack == null) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载发电机" + s + "的燃料" + key + "时遇到了问题: " + "输入物品为空或格式错误，已跳过加载");
                continue;
            }
            int seconds = section1.getInt("seconds");

            if (seconds < 1) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载发电机" + s + "的燃料" + key + "时遇到了问题: " + "秒数不能小于1，已跳过加载");
                continue;
            }

            ItemStack output = null;
            if (section1.contains("output")) {
                ConfigurationSection outputSet = section1.getConfigurationSection("output");
                output = CommonUtils.readItem(outputSet, true, addon);
                if (output == null) {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载发电机" + s + "的燃料" + key + "时遇到了问题: " + "输出物品为空或格式错误，已转为空");
                }
            }

            MachineFuel fuel = new MachineFuel(seconds, stack, output);
            fuels.add(fuel);
        }
        return fuels;
    }
}
