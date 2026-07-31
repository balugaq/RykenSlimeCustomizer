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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.LockedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.BaseRSCItemGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.GroupType;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.RSCItemGroupLegacy;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.Visible;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class ItemGroupReader extends YamlReader<ItemGroup> {
    public ItemGroupReader(YamlConfiguration config, ProjectAddon addon) {
        super(config, addon);
    }

    @Override
    public ItemGroup readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        ExceptionHandler.HandleResult conflict = ExceptionHandler.handleGroupIdConflict(s);

        if (conflict == ExceptionHandler.HandleResult.FAILED) return null;

        ConfigurationSection item = section.getConfigurationSection("item");
        ItemStack stack = CommonUtils.readItem(item, false, addon);
        if (stack == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品组" + s + "时遇到了问题: " + "物品为空或格式错误导致无法加载");
            return null;
        }

        String type = section.getString("type", "");
        GroupType groupType = GroupType.getType(type);
        if (groupType == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品组" + s + "时遇到了问题: " + "物品组类型" + type + "无效");
            return null;
        }
        NamespacedKey key = new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, s);

        int tier = section.getInt("tier", 3);

        boolean forceHidden = section.getBoolean("forceHidden", false);

        BaseRSCItemGroup parent = null;
        var par = section.getString("parent");
        if (par != null) {
            var parK = NamespacedKey.fromString(par.toLowerCase(), RykenSlimefunCustomizer.INSTANCE);
            ItemGroup raw = CommonUtils.getIf(Slimefun.getRegistry().getAllItemGroups(), ig -> ig.getKey().equals(parK));
            switch (raw) {
                case null -> {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品组" + section.getCurrentPath() + "时遇到了问题: 无法找到父物品组: " + par);
                    return null;
                }
                case NestedItemGroup nig -> {
                    if (groupType == GroupType.locked) {
                        ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品组" + section.getCurrentPath() + "时遇到了问题: 无法将 LockedItemGroup 添加到 NestedItemGroup 中: " + par);
                        return null;
                    }
                    ExceptionHandler.debugLog(() -> "由于技术限制原因，物品组 " + key + " 无法成为可嵌套物品组，因为其父物品组为 NestedItemGroup");
                    SubItemGroup group = new SubItemGroup(key, nig, stack, tier);
                    nig.addSubGroup(group);
                    group.register(RykenSlimefunCustomizer.INSTANCE);
                    return group;
                }
                case RSCItemGroupLegacy rsc -> parent = rsc;
                default -> {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载物品组" + section.getCurrentPath() + "时遇到了问题: 无法将添加到指定的物品组: " + par);
                    return null;
                }
            }

        }

        if (groupType == GroupType.locked) {
            List<NamespacedKey> parents = new ArrayList<>();
            for (String ig : section.getStringList("parents")) {
                NamespacedKey nk = NamespacedKey.fromString(ig.toLowerCase());
                if (nk == null) {
                    ExceptionHandler.handleWarning("在附属" + addon.getAddonId() + "中加载物品组" + s + "时遇到了问题: "
                        + ig + "不是一个有效的NamespacedKey");
                    continue;
                }
                parents.add(nk);
            }
            ExceptionHandler.debugLog(() -> "由于技术限制原因，物品组 LockedItemGroup: " + key + " 无法成为可嵌套物品组");
            ItemGroup group = new LockedItemGroup(key, stack, tier, parents.toArray(new NamespacedKey[0]));
            if (parent != null) {
                parent.addContent(group);
            }
            group.register(RykenSlimefunCustomizer.INSTANCE);
            return group;
        }

        Visible visible;
        if (groupType == GroupType.seasonal) {
            int month = section.getInt("month", 1);
            visible = (a, b, c) -> month == LocalDate.now().getMonth().getValue();
        } else {
            visible = (a, b, c) -> true;
        }

        BaseRSCItemGroup group = BaseRSCItemGroup.create(key, stack, tier, addon, groupType, visible, forceHidden, parent != null);

        if (parent != null) {
            parent.addContent(group);
        }

        if (groupType == GroupType.button) {
            for (var action : section.getStringList("actions")) {
                group.addContent(action);
            }
        }

        group.register(RykenSlimefunCustomizer.INSTANCE);

        return group.getSelf();
    }

    // 物品组不需要预加载物品
    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return List.of();
    }
}
