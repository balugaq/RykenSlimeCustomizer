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
package org.lins.mmmjjkx.rykenslimefuncustomizer.readers;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.LockedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.BaseRSCItemGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.GroupType;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.RSCItemGroupJEG;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.RSCItemGroupLegacy;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.Visible;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Keys;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ItemGroupReader extends YamlReader<ItemGroup> {
    @Override
    public String getFileName() {
        return Constants.GROUPS_FILE;
    }

    public ItemGroupReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public ItemGroup readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        if (!CommonUtils.passItemGroupIdConflictCheck(s)) return null;

        ConfigurationSection item = section.getConfigurationSection("item");
        ItemStack stack = CommonUtils.readItem(file, item, addon);
        if (stack == null) {
            Debug.error(file, section, "缺少或配置错误 '物品' (item)");
            return null;
        }

        String type = section.getString("type", "");
        GroupType groupType = GroupType.getType(type);
        if (groupType == null) {
            Debug.error(file, section, "缺少或配置错误 '物品组类型' (type): " + type);
            return null;
        }
        NamespacedKey key = Keys.newKey(s);

        int tier = section.getInt("tier", 3);

        boolean forceHidden = section.getBoolean("forceHidden", false);

        BaseRSCItemGroup parent = null;
        var par = section.getString("parent");
        if (par != null) {
            var parK = NamespacedKey.fromString(par.toLowerCase(), RykenSlimefunCustomizer.INSTANCE);
            ItemGroup raw = CommonUtils.getIf(Slimefun.getRegistry().getAllItemGroups(), ig -> ig.getKey().equals(parK));
            switch (raw) {
                case null -> {
                    Debug.error(file, section, "无法找到父物品组 (parent): " + par);
                    return null;
                }
                case NestedItemGroup nig -> {
                    if (groupType == GroupType.locked) {
                        Debug.error(file, section, "无法将 LockedItemGroup 添加到 NestedItemGroup 中 (parent): " + par);
                        return null;
                    }
                    Debug.debug(() -> "由于技术限制原因，物品组 " + key + " 无法成为可嵌套物品组，因为其父物品组为 NestedItemGroup");
                    SubItemGroup group = new SubItemGroup(key, nig, stack, tier);
                    nig.addSubGroup(group);
                    group.register(RykenSlimefunCustomizer.INSTANCE);
                    return group;
                }
                case RSCItemGroupLegacy rsc -> parent = rsc;
                case RSCItemGroupJEG rsc -> parent = rsc;
                default -> {
                    Debug.error(file, section, "无法添加当前物品组至到指定的物品组 (parent): " + key + " -> " + par);
                    return null;
                }
            }

        }

        if (groupType == GroupType.locked) {
            List<NamespacedKey> parents = new ArrayList<>();
            for (String ig : section.getStringList("parents")) {
                NamespacedKey nk = NamespacedKey.fromString(ig.toLowerCase());
                if (nk == null) {
                    Debug.warn(file, section, "NamespacedKey 非法 (parents): " + ig);
                    continue;
                }
                parents.add(nk);
            }
            Debug.debug(() -> "由于技术限制原因，物品组 LockedItemGroup: " + key + " 无法成为可嵌套物品组");
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
