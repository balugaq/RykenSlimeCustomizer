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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.item;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.attributes.PiglinBarterDrop;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactivity;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.attributes.Soulbound;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.RainbowTickHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ToolUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.WeaponUseHandler;
import io.github.thebusybiscuit.slimefun4.utils.ColoredMaterial;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import lombok.SneakyThrows;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.item.CustomDefaultItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.parent.CustomItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.global.DropFromBlock;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.WitherProofBlockImpl;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ClassUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemReader extends YamlReader<SlimefunItem> {
    @Override
    public String getFileName() {
        return Constants.ITEMS_FILE;
    }

    public ItemReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    private CustomItem resolveRadiation(CustomItem instance, BaseResult base, ConfigurationSection section, Object[] constructorArgs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        String radio = section.getString("radiation");
        boolean addRadiationLore = section.getBoolean("add_radiation_lore", true);
        Optional<Radioactivity> radioactivity = CommonUtils.getEnum(Radioactivity.class, radio);
        if (radioactivity.isEmpty()) {
            Debug.warning(file, section, "错误的辐射等级级别: " + radio + " 已跳过");
            return instance;
        }

        if (addRadiationLore) {
            CommonUtils.addLore(base.sfis(), true, LoreBuilder.radioactive(radioactivity.get()));
        }

        Class<? extends CustomItem> clazz = ClassUtils.generateClass(
            instance.getClass(),
            "Radiation",
            "Item",
            new Class[] {Radioactive.class},
            builder -> builder.method(ElementMatchers.isDeclaredBy(Radioactive.class))
                .intercept(FixedValue.value(radioactivity.get())));

        return (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
    }

    @SneakyThrows
    @Override
    public SlimefunItem readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        var base = getBase(section, s);
        if (base == null) return null;

        JavaScriptEval eval = getScriptOrNull(section, section.getString("script"));

        CustomItem instance = new CustomDefaultItem(base);

        Object[] constructorArgs = instance.constructorArgs();

        if (section.contains("rainbow")) {
            String materialType = section.getString("rainbow", "");
            if (!base.sfis().getType().isBlock()) {
                Debug.warning(file, section, "非方块无法设置彩虹属性 (rainbow) 已跳过");
            } else {
                if (materialType.equalsIgnoreCase("CUSTOM")) {
                    List<String> materials = section.getStringList("rainbow_materials");
                    if (materials.isEmpty()) {
                        Debug.warning(file, section, "未设置彩虹属性材料 (rainbow_materials) 已跳过");
                    } else {
                        List<Material> colorMaterials = new ArrayList<>();

                        for (String materialS : materials) {
                            Optional<Material> material = CommonUtils.getMaterial(materialS);
                            if (material.isEmpty()) {
                                Debug.warning(file, section, "错误的彩虹属性材料 (rainbow_materials): " + materialS + " 已跳过");
                                continue;
                            }
                            colorMaterials.add(material.get());
                        }

                        instance.addItemHandler(new RainbowTickHandler(colorMaterials));
                    }
                } else {
                    Optional<ColoredMaterial> cm = CommonUtils.getEnum(ColoredMaterial.class, materialType);
                    if (cm.isEmpty()) {
                        Debug.error(file, section, "无法识别可染色材料类型 (rainbow): " + materialType);
                        return null;
                    }

                    instance.addItemHandler(new RainbowTickHandler(cm.get()));
                }
            }
        }

        if (section.getBoolean("placeable", false)) {
            Class<? extends CustomItem> clazz = ClassUtils.generateClass(
                instance.getClass(),
                "NotPlaceable",
                "Item",
                new Class[] {NotPlaceable.class},
                null
            );

            instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        if (section.getBoolean("anti_wither", false)) {
            if (!base.sfis().getType().isBlock()) {
                Debug.warning(file, section, "非方块无法设置防凋零属性 已跳过");
            } else {
                Class<? extends CustomItem> clazz = ClassUtils.generateClass(
                    instance.getClass(),
                    "WitherProof",
                    "Item",
                    new Class[]{WitherProofBlockImpl.class},
                    null
                );

                instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
            }
        }

        if (section.getBoolean("soulbound", false)) {
            Class<? extends CustomItem> clazz = ClassUtils.generateClass(
                instance.getClass(),
                "Soulbound",
                "Item",
                new Class[] {Soulbound.class},
                null
            );

            instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        if (section.contains("piglin_trade_chance")) {
            int chance = CommonUtils.clamp(section.getInt("chance", 100), 1, 100, file, section, "'猪灵交易概率 (piglin_trade_chance) 非法'");

            Class<? extends CustomItem> clazz = ClassUtils.generateClass(
                instance.getClass(),
                "PiglinBarterDrop",
                "Item",
                new Class[] {PiglinBarterDrop.class},
                builder -> builder.method(ElementMatchers.isDeclaredBy(PiglinBarterDrop.class))
                        .intercept(FixedValue.value(chance)));

            instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);
        }

        if (section.contains("energy_capacity")) {
            resolveEnergyCapacity(section, instance, eval, base, constructorArgs);
        }

        if (section.contains("radiation")) {
            instance = resolveRadiation(instance, base, section, constructorArgs);
        }

        boolean hidden = section.getBoolean("hidden", false);
        if (hidden) instance.setHidden(true);

        instance.setUseableInWorkbench(section.getBoolean("vanilla", false));

        if (section.contains("drop_from")) {
            resolveDropFrom(section, base);
        }

        instance.register(RykenSlimefunCustomizer.INSTANCE);

        return instance;
    }

    private CustomItem resolveEnergyCapacity(ConfigurationSection section, CustomItem instance, @Nullable ScriptEval eval, BaseResult base, Object[] constructorArgs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        double energyCapacity = section.getDouble("energy_capacity");
        if (energyCapacity < 1) {
            Debug.warning(file, section, "能源容量 (energy_capacity) 超出范围", 1.0d, Float.MAX_VALUE);
            return null;
        }

        CommonUtils.addLore(base.sfis(), true, CMIChatColor.translate("&8⇨ &e⚡ &70 / " + energyCapacity + " J"));

        Class<? extends CustomItem> clazz = ClassUtils.generateClass(
            instance.getClass(),
            "Rechargeable",
            "Item",
            new Class[] {Rechargeable.class},
            builder -> builder.method(ElementMatchers.isDeclaredBy(Rechargeable.class))
                .intercept(FixedValue.value((float) energyCapacity)));

        instance = (CustomItem) clazz.getDeclaredConstructors()[0].newInstance(constructorArgs);


        if (eval != null) {
            eval.doInit();

            instance.addItemHandler((ItemUseHandler) e -> {
                eval.evalFunction("onUse", e, this);
                e.cancel();
            });

            instance.addItemHandler((WeaponUseHandler) (e, p, it) -> {
                eval.evalFunction("onWeaponHit", e, p, it);
            });
            instance.addItemHandler((ToolUseHandler) (e, it, i, drops) -> eval.evalFunction("onToolUse", e, it, i, drops));
        } else {
            instance.addItemHandler((ItemUseHandler) PlayerRightClickEvent::cancel);
        }
        return instance;
    }

    private void resolveDropFrom(ConfigurationSection section, BaseResult base) {
        int chance = CommonUtils.clamp(section.getInt("chance", 100), 1, 100, file, section, "'概率 (chance) 非法'");;
        int amount = section.isInt("drop_amount") ? section.getInt("drop_amount", 1) : -1;

        String dropMaterial = section.getString("drop_from", "");
        Optional<Material> xm = CommonUtils.getMaterial(dropMaterial);
        if (xm.isEmpty()) {
            Debug.warning(file, section, "掉落方块材料类型 (drop_from) 无效 已跳过");
            return;
        }
        Material material = xm.get();
        if (amount != -1) {
            DropFromBlock.addDrop(material, new DropFromBlock.Drop(base.sfis(), chance, addon, amount, amount));
            return;
        }

        int min, max;
        resolve_amount:
        {
            String between = section.getString("drop_amount", "1");
            if (between.contains("-")) {
                String[] split = between.split("-");
                if (split.length != 2) {
                    Debug.warning(file, section, "掉落数量区间 (drop_amount) 非法，已将掉落数量转为 " + base.sfis().getAmount());
                    min = max = base.sfis().getAmount();
                    break resolve_amount;
                }

                try {
                    min = Integer.parseInt(split[0]);
                    max = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    Debug.warning(file, section, "掉落数量区间 (drop_amount) 非法，已将掉落数量转为 " + base.sfis().getAmount());
                    min = max = base.sfis().getAmount();
                }
            } else {
                try {
                    min = max = Integer.parseInt(between);
                } catch (NumberFormatException e) {
                    Debug.warning(file, section, "掉落数量 (drop_amount) 非法，已将掉落数量转为 " + base.sfis().getAmount());
                    min = max = 1;
                }
            }
        }

        DropFromBlock.addDrop(material, new DropFromBlock.Drop(base.sfis(), chance, addon, min, max));
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return anyPreloadItems(s);
    }
}
