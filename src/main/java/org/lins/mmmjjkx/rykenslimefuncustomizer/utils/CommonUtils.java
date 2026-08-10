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
package org.lins.mmmjjkx.rykenslimefuncustomizer.utils;

import io.github.projectunified.uniitem.all.AllItemProvider;
import io.github.projectunified.uniitem.api.ItemKey;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import lombok.SneakyThrows;
import net.guizhanss.guizhanlib.minecraft.utils.compatibility.EnchantmentX;
import net.guizhanss.guizhanlib.minecraft.utils.compatibility.ItemFlagX;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class CommonUtils {
    private static final Map<String, String> materialMappings = Map.of(
        "GRASS", "SHORT_GRASS",
        "SHORT_GRASS", "GRASS",
        "SCUTE", "TURTLE_SCUTE",
        "TURTLE_SCUTE", "SCUTE",
        "CHAIN", "IRON_CHAIN",
        "IRON_CHAIN", "CHAIN"
    );

    @Nullable public static <T> T getIf(Iterable<T> iterable, Predicate<T> filter) {
        if (iterable == null) return null;

        for (T t : iterable) {
            if (filter.test(t)) {
                return t;
            }
        }
        return null;
    }

    public static Optional<Material> getMaterial(String s) {
        Material m = Material.matchMaterial(s);
        if (m == null) {
            var m2 = materialMappings.get(s);
            return Optional.ofNullable(Material.matchMaterial(m2));
        }
        return Optional.of(m);
    }

    @NotNull public static ItemStack[] readRecipe(File file, ConfigurationSection section, ProjectAddon addon) {
        return readRecipe(file, section, addon, 9);
    }

    @NotNull
    public static ItemStack[] readRecipe(File file, ConfigurationSection section, @NotNull ProjectAddon addon, int size) {
        if (section == null) return new ItemStack[size];
        ItemStack[] itemStacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            ConfigurationSection section1 = section.getConfigurationSection(String.valueOf(i + 1));
            itemStacks[i] = readItem(file, section1, addon);
        }
        return itemStacks;
    }

    @SneakyThrows
    @Nullable public static ItemStack readItem(File file, ConfigurationSection section, ProjectAddon addon) {
        if (section == null) return null;

        String type = section.getString("material_type", "mc");
        if (!type.equalsIgnoreCase("none") && !section.contains("material")) {
            Debug.error(file, section, "你设置了材料类型，但没有设置对应的材料! (material)");
            return null;
        }

        String material = section.getString("material", "");
        List<String> lore = CMIChatColor.translate(section.getStringList("lore"));
        String name = CMIChatColor.translate(section.getString("name", ""));
        boolean glow = section.getBoolean("glow", false);
        boolean hasEnchantment = section.contains("enchantments") && section.isList("enchantments");
        int modelId = section.getInt("modelId");
        int amount = section.getInt("amount", 1);

        return readItem(file, section, addon, type, material.trim(), name, lore, glow, hasEnchantment, modelId, amount);
    }

    private static void tryReadColor(File file, ConfigurationSection section, ItemMeta meta) {
        String color = section.getString("color");
        if (color == null) return; // skip

        String[] parts = color.split(",");
        if (parts.length != 3) {
            Debug.warning(file, section, "物品颜色 (color) 非法: " + Arrays.toString(parts) + " 已跳过");
            return;
        }

        try {
            Color bkcolor = Color.fromRGB(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));

            switch (meta) {
                case LeatherArmorMeta lam -> {
                    lam.setColor(bkcolor);
                }
                case PotionMeta pm -> {
                    pm.setColor(bkcolor);
                    pm.addItemFlags(ItemFlagX.HIDE_ADDITIONAL_TOOLTIP);
                }
                case FireworkEffectMeta fem -> {
                    fem.setEffect(FireworkEffect.builder()
                        .withColor(bkcolor)
                        .build());
                    fem.addItemFlags(ItemFlagX.HIDE_ADDITIONAL_TOOLTIP);
                }
                default -> {
                    Debug.warning(file, section, "物品不支持使用物品颜色 (color): " + meta.getClass().getSimpleName() + " 已跳过");
                }
            }
        } catch (NumberFormatException e) {
            ExceptionHandler.handleError("物品颜色 (color) 非法: " + Arrays.toString(parts));
        }
    }

    @Nullable
    private static ItemStack getBaseItemStack(File file, ConfigurationSection section, String type, String material, ProjectAddon addon) {
        switch (type.toLowerCase()) {
            case "none" -> {
                return new ItemStack(Material.AIR);
            }
            case "skull_hash" -> {
                PlayerSkin playerSkin = PlayerSkin.fromHashCode(material);
                ItemStack head = PlayerHead.getItemStack(playerSkin);
                return new CustomItemStack(head);
            }
            case "skull_base64", "skull" -> {
                PlayerSkin playerSkin = PlayerSkin.fromBase64(material);
                ItemStack head = PlayerHead.getItemStack(playerSkin);
                return new CustomItemStack(head);
            }
            case "skull_url" -> {
                PlayerSkin playerSkin = PlayerSkin.fromURL(material);
                ItemStack head = PlayerHead.getItemStack(playerSkin);
                return new CustomItemStack(head);
            }
            case "slimefun", "sf" -> {
                SlimefunItemStack sfis = addon.getPreloadItems().get(material.toUpperCase());
                if (sfis != null) return sfis.clone();

                SlimefunItem sfItem = SlimefunItem.getById(material.toUpperCase());
                if (sfItem != null) {
                    return sfItem.getItem().clone();
                } else {
                    Debug.error(file, section, "无法找到粘液物品: " + material);
                    return null;
                }
            }
            case "uniitem" -> {
                try {
                    AllItemProvider provider = new AllItemProvider();
                    String[] split = material.split("::");
                    ItemStack item = provider.item(new ItemKey(split[0], split[1]));
                    if (item == null) {
                        Debug.error(file, section, "无法读取 UniItem 物品!");
                        return null;
                    }

                    item.setAmount(1);

                    return item;
                } catch (NoClassDefFoundError e) {
                    Debug.error(file, section, "无法加载 UniItem 依赖! 无法识别物品.");
                    return null;
                }
            }
            case "saveditem" -> {
                File saveditemFile = new File(addon.getSavedItemsFolder(), material + ".yml");
                if (!saveditemFile.exists()) {
                    Debug.error(file, section, "保存物品对应的文件不存在: " + material);
                    return null;
                }

                var cfg = YamlConfiguration.loadConfiguration(saveditemFile);
                var itemCfg = cfg.getConfigurationSection("item");
                if (itemCfg == null) {
                    Debug.error(file, section, "无法识别对应的保存物品: " + material);
                    return null;
                }

                if (itemCfg.contains("v")) {
                    itemCfg.set("v", Bukkit.getUnsafe().getDataVersion());
                }

                ItemStack itemStack = cfg.getItemStack("item");
                if (itemStack != null) {
                    return itemStack;
                } else {
                    Debug.error(file, section, "无法识别对应的保存物品: " + material);
                    return null;
                }
            }
            case "mc", "minecraft", "vanilla" -> {
                Optional<Material> mat = getMaterial(material);
                if (mat.isEmpty()) {
                    Debug.error(file, section, "无法识别粘液物品: " + material);
                    return null;
                }

                CustomItemStack stack = new CustomItemStack(mat.get());
                stack.editMeta(meta -> {
                    tryReadColor(file, section, meta);
                });

                return stack;
            }
            default -> {
                Debug.warning(file, section, "无法识别的类型: " + type + " 尝试以原版物品重新加载...");
                var mc = getBaseItemStack(file, section, "mc", material, addon);
                if (mc != null) return mc;
                Debug.warning(file, section, "无法识别的类型: " + type + " 尝试以粘液物品重新加载...");
                var sf = getBaseItemStack(file, section, "slimefun", material, addon);
                if (sf != null) return sf;
                Debug.error(file, section, "无法识别的类型: " + type + " 无法加载!");
                return null;
            }
        }
    }

    @SneakyThrows
    @SuppressWarnings("deprecation")
    public static ItemStack readItem(
            File file,
            ConfigurationSection section,
            ProjectAddon addon,
            String type,
            String material,
            String name,
            List<String> lore,
            boolean glow,
            boolean hasEnchantment,
            int modelId,
            int amount) {

        if (material.startsWith("ey") || material.startsWith("ew")) {
            type = "skull";
        } else if (material.startsWith("http") || material.startsWith("https")) {
            type = "skull_url";
        } else if (material.matches("^[0-9A-Fa-f]{64}+$")) {
            type = "skull_hash";
        }

        String finalType = type;
        ItemStack itemStack = CommonUtils.readPipe(material, s -> getBaseItemStack(file, section, finalType, material, addon));
        if (itemStack == null) {
            Debug.warning(file, section, "无法识别对应的物品，已转为石头.");
            itemStack = createDefaultItem();
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (modelId > 0) meta.setCustomModelData(modelId);
        if (!name.isBlank()) meta.setDisplayName(name);
        if (!lore.isEmpty()) meta.setLore(lore);

        itemStack.setItemMeta(meta);

        if (amount > 100 || amount < -1) {
            Debug.warning(file, section, "物品数量超出范围 (amount): " + amount, -1, 100);
        } else {
            itemStack.setAmount(amount);
        }

        if (hasEnchantment) {
            List<String> enchants = section.getStringList("enchantments");
            for (String enchant : enchants) {
                String[] s2 = enchant.split(" ");
                if (s2.length != 2) {
                    Debug.warning(file, section, "附魔格式非法 (enchantments): " + enchant + " 已跳过");
                    continue;
                }

                String enchantName = s2[0];

                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                if (enchantment == null) {
                    Debug.warning(file, section, "未知的附魔 (enchantments): " + enchantName + " 已跳过");
                    continue;
                }

                try {
                    int lvl = Integer.parseInt(s2[1]);
                    itemStack.addUnsafeEnchantment(enchantment, lvl);
                } catch (NumberFormatException e) {
                    Debug.warning(file, section, "附魔格式非法 (enchantments): " + enchant + " 已跳过");
                    continue;
                }
            }
        }

        if (glow) {
            itemStack.addUnsafeEnchantment(EnchantmentX.LUCK_OF_THE_SEA, 1);
            itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return itemStack;
    }

    public static CustomItemStack createDefaultItem() {
        return new CustomItemStack(Material.STONE);
    }

    @SuppressWarnings("deprecation")
    public static void addLore(ItemStack stack, boolean emptyLine, String... lore) {
        ItemMeta im = stack.getItemMeta();
        var lorel = im.getLore();
        if (lorel != null) {
            if (emptyLine) {
                lorel.add("");
            }
            lorel.addAll(CMIChatColor.translate(Arrays.asList(lore)));
        } else {
            lorel = CMIChatColor.translate(Arrays.asList(lore));
        }
        im.setLore(lorel);
        stack.setItemMeta(im);
    }

    public static void saveItem(ItemStack item, String fileName, ProjectAddon addon) {
        File folder = addon.getSavedItemsFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName + ".yml");
        if (!file.exists()) {
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        YamlConfiguration configuration = new YamlConfiguration();

        configuration.set("item", item);

        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void completeFile(String resourceFile) {
        JavaPlugin plugin = RykenSlimefunCustomizer.INSTANCE;

        InputStream stream = plugin.getResource(resourceFile);
        File file = new File(plugin.getDataFolder(), resourceFile);
        if (!file.exists()) {
            if (stream != null) {
                plugin.saveResource(resourceFile, false);
                return;
            }
            return;
        }
        if (stream == null) {
            Debug.error("无法找到文件 " + resourceFile + " 请检查插件文件是否损坏!");
            return;
        }
        try {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
            YamlConfiguration configuration2 = new YamlConfiguration();
            configuration2.load(file);

            completeFile0(configuration, configuration2);
            configuration2.save(file);
        } catch (Exception e) {
            Debug.error("无法找到文件 " + resourceFile + " 的同步，请检查插件文件是否损坏!", e);
        }
    }

    public static void completeFile(YamlConfiguration origin, YamlConfiguration dest) {
        completeFile0(origin, dest);
    }

    private static void completeFile0(YamlConfiguration origin, YamlConfiguration dest) {
        for (String key : origin.getKeys(true)) {
            Object value = origin.get(key);
            if (value instanceof List<?>) {
                List<?> list2 = dest.getList(key);
                if (list2 == null) {
                    dest.set(key, value);
                    continue;
                }
            }

            if (!dest.contains(key)) {
                dest.set(key, value);
            }
        }
    }

    public static int versionToCode(String s) {
        String[] ver = s.split("\\.");
        String ver2 = "";
        for (String v : ver) {
            ver2 = ver2.concat(v);
        }

        if (ver.length == 2) {
            ver2 = ver2.concat("0");
        }

        return Integer.parseInt(ver2);
    }

    public static String richFormatSeconds(int seconds) {
        String lore = "&e制作时间: &b" + seconds + "&es";
        if (seconds > 60) {
            lore = lore.concat("(" + CommonUtils.formatSeconds(seconds) + "&e)");
        }
        return lore;
    }

    public static String formatSeconds(int seconds) {
        if (seconds < 60) {
            return "&b" + seconds + "&es";
        } else if (seconds > 60 && seconds < 3600) {
            int m = seconds / 60;
            int s = seconds % 60;
            return "&b" + m + "&emin" + (s != 0 ? "&b" + s + "&es" : "");
        } else {
            int h = seconds / 3600;
            int m = (seconds % 3600) / 60;
            int s = (seconds % 3600) % 60;
            return "&b" + h + "&eh" + (m != 0 ? "&b" + m + "&emin" : "") + (s != 0 ? "&b" + s + "&es" : "");
        }
    }

    public static ItemStack[] removeNulls(ItemStack[] origin) {
        int count = 0;
        for (ItemStack element : origin) {
            if (element != null) {
                count++;
            }
        }

        ItemStack[] newArray = new ItemStack[count];

        int index = 0;
        for (ItemStack element : origin) {
            if (element != null) {
                newArray[index] = element;
                index++;
            }
        }

        return newArray;
    }

    @Nullable
    public static <T> T readPipe(String s, Function<String, @Nullable T> parser) {
        for (String part : Arrays.stream(s.split("\\|")).map(String::trim).toList()) {
            T r = parser.apply(part);;
            if (r != null) return r;
        }
        return null;
    }

    public static int clamp(int v, int a, int b, File file, ConfigurationSection section, String msg) {
        if (v < a) {
            Debug.warning(file, section, msg + "，已转为 " + a, a, b);
            v = a;
        }

        if (v > b) {
            Debug.warning(file, section, msg + "，已转为 " + b, a, b);
            v = b;
        }

        return v;
    }

    public static float clamp(float v, float a, float b, File file, ConfigurationSection section, String msg) {
        if (v < a) {
            Debug.warning(file, section, msg + "，已转为 " + a, a, b);
            v = a;
        }

        if (v > b) {
            Debug.warning(file, section, msg + "，已转为 " + b, a, b);
            v = b;
        }

        return v;
    }

    public static float clamp(float v, float a, float def, float b, File file, ConfigurationSection section, String msg) {
        if (v < a) {
            Debug.warning(file, section, msg + "，已转为 " + def, a, b);
            v = a;
        }

        if (v > b) {
            Debug.warning(file, section, msg + "，已转为 " + def, a, b);
            v = b;
        }

        return v;
    }

    public static <T extends Enum<T>> Optional<T> getEnum(Class<T> enumClass, String name) {
        return readPipe(name, n -> {
            try {
                var values = enumClass.getEnumConstants();
                if (values == null) return Optional.empty();
                T bValue = null;
                for (T enumValue : values) {
                    // 模糊匹配
                    if (enumValue.name().equalsIgnoreCase(name)) {
                        bValue = enumValue;
                    }
                    if (enumValue.name().equals(name)) {
                        return Optional.of(enumValue);
                    }
                }
                return Optional.ofNullable(bValue);
            } catch (NullPointerException | IllegalArgumentException ignored) {
                return Optional.empty();
            }
        });
    }
}
