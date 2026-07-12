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

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

public class RecipeTypesReader extends YamlReader<RecipeType> {
    public RecipeTypesReader(YamlConfiguration config, ProjectAddon addon) {
        super(config, addon);
    }

    @Override
    public RecipeType readEach(String s) {
        ConfigurationSection configurationSection = configuration.getConfigurationSection(s);
        if (configurationSection == null) return null;

        ItemStack item = CommonUtils.readItem(configurationSection, false, addon);
        if (item == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载配方类型" + s + "时遇到了问题: " + "物品为空或格式错误导致无法加载");
            return null;
        }

        return new RecipeType(new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, s.toLowerCase()), item);
    }

    // 配方类型不需要预加载物品
    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return List.of();
    }
}
