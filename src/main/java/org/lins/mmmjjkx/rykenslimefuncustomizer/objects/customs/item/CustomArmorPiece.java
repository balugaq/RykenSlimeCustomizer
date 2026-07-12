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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.item;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.ProtectionType;
import io.github.thebusybiscuit.slimefun4.core.attributes.ProtectiveArmor;
import io.github.thebusybiscuit.slimefun4.implementation.items.armor.SlimefunArmorPiece;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

public class CustomArmorPiece extends SlimefunArmorPiece implements ProtectiveArmor {
    private final String armorKey;
    private final boolean fullSet;
    private final ProtectionType[] protectionTypes;
    private final String projectId;

    public CustomArmorPiece(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            @Nullable PotionEffect[] effects,
            boolean fullSet,
            String armorKey,
            ProtectionType[] protectionTypes,
            String projectId) {
        super(itemGroup, item, recipeType, recipe, effects);

        this.armorKey = armorKey;
        this.fullSet = fullSet;
        this.protectionTypes = protectionTypes;
        this.projectId = projectId;

        register(RykenSlimefunCustomizer.INSTANCE);
    }

    @Override
    public @NotNull ProtectionType[] getProtectionTypes() {
        return protectionTypes;
    }

    @Override
    public boolean isFullSetRequired() {
        return fullSet;
    }

    @Nullable @Override
    public NamespacedKey getArmorSetId() {
        return new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, projectId + "_" + armorKey);
    }
}
