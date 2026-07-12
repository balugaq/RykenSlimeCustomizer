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

import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import java.util.function.BiFunction;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;

public class CustomGeoResource extends UnplaceableBlock implements GEOResource {
    private final BiFunction<World.Environment, Biome, Integer> supply;
    private final int maxDeviation;
    private final boolean obtainableFromGEOMiner;
    private final String name;

    public CustomGeoResource(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType type,
            ItemStack[] recipe,
            BiFunction<World.Environment, Biome, Integer> supply,
            int maxDeviation,
            boolean obtainableFromGEOMiner,
            String name) {
        super(itemGroup, item, type, recipe);

        this.supply = supply;
        this.maxDeviation = maxDeviation;
        this.obtainableFromGEOMiner = obtainableFromGEOMiner;
        this.name = name;

        register();
        register(RykenSlimefunCustomizer.INSTANCE);
    }

    @Override
    public int getDefaultSupply(@NotNull World.Environment environment, @NotNull Biome biome) {
        return supply.apply(environment, biome);
    }

    @Override
    public int getMaxDeviation() {
        return maxDeviation;
    }

    @NotNull @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isObtainableFromGEOMiner() {
        return obtainableFromGEOMiner;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(RykenSlimefunCustomizer.INSTANCE, getId());
    }
}
