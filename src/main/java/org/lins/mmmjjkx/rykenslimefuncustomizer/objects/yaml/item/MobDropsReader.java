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

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.item.exts.CustomMobDrop;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class MobDropsReader extends YamlReader<CustomMobDrop> {

    @Override
    public String getFileName() {
        return Constants.MOB_DROPS_FILE;
    }

    public MobDropsReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public CustomMobDrop readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        String id = addon.getId(s, section.getString("id_alias"));

        ExceptionHandler.HandleResult result = ExceptionHandler.handleIdConflict(id);
        if (result == ExceptionHandler.HandleResult.FAILED) return null;

        String igId = section.getString("item_group");

        Pair<ExceptionHandler.HandleResult, ItemGroup> group = ExceptionHandler.handleItemGroupGet(addon, igId);
        if (group.getFirstValue() == ExceptionHandler.HandleResult.FAILED) return null;

        SlimefunItemStack sfis = getPreloadItem(id);
        if (sfis == null) return null;

        String type = section.getString("entity");

        Optional<EntityType> entity = CommonUtils.getEnum(EntityType.class, type);
        if (entity.isEmpty()) {
            Debug.error(file, section, "错误的生物类型 (entity): " + type);
            return null;
        }

        EntityType entityType = entity.get();

        Material eggMaterial = CommonUtils.getEnum(Material.class, entityType + "_SPAWN_EGG").orElse(Material.EGG);
        int chance = CommonUtils.clamp(section.getInt("chance", 100), 1, 100, file, section, "'概率 (chance) 非法'");

        Component lore = t("&a击杀 ")
                .append(t("&b"))
                .append(Component.translatable(entityType.translationKey()))
                .append(t(" &a时会有"))
                .append(t(" &b " + chance + "%"))
                .append(t(" &a的概率掉落"));

        ItemStack itemStack = new CustomItemStack(eggMaterial, meta -> {
            meta.lore(List.of(lore));
        });
        ItemStack[] recipe = new ItemStack[] {null, null, null, null, itemStack, null, null, null, null};

        return new CustomMobDrop(group.getSecondValue(), sfis, recipe, chance, entityType, sfis);
    }

    public Component t(String s) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return anyPreloadItems(s);
    }
}
