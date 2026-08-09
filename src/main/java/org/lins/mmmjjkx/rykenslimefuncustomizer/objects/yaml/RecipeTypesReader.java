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
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomRecipeType;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine.MultiBlockMachineReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.List;

public class RecipeTypesReader extends YamlReader<RecipeType> {
    @Override
    public String getFileName() {
        return Constants.RECIPE_TYPES_FILE;
    }

    public RecipeTypesReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public RecipeType readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;

        ItemStack item = CommonUtils.readItem(section, false, addon);
        if (item == null) {
            Debug.error(file, section, "缺少或配置错误 '物品' (item)");
            return null;
        }

        String bindToMultiblock = section.getString("bind-to-multiblock");
        if (bindToMultiblock != null) {
            return new CustomRecipeType(new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, s.toLowerCase()), item, (recipe, result) -> {
                MultiBlockMachineReader.addPreaddRecipe(bindToMultiblock, recipe, result);
            }, (a, b) -> {/* unregister recipe is not supported yet*/});
        }

        return new CustomRecipeType(new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, s.toLowerCase()), item);
    }

    // 配方类型不需要预加载物品
    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return List.of();
    }
}
