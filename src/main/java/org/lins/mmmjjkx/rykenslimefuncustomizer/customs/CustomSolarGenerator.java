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
package org.lins.mmmjjkx.rykenslimefuncustomizer.customs;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.generators.SolarGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.BaseRSCItemGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.super_multiblock.SuperMultiBlockManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;

public class CustomSolarGenerator extends SolarGenerator {
    @Override
    public void load() {
        if (!hidden) {
            BaseRSCItemGroup.addItemToGroup(getItemGroup(), this);
        }

        getRecipeType().register(getRecipe(), getRecipeOutput());
    }
    private final int lightLevel;

    public CustomSolarGenerator(
            YamlReader.BaseResult base,
            int dayEnergy,
            int nightEnergy,
            int capacity,
            int lightLevel) {
        super(base.itemGroup(), dayEnergy, nightEnergy, base.sfis(), base.recipeType(), base.recipe(), capacity);

        this.lightLevel = lightLevel;

        register(RykenSlimefunCustomizer.INSTANCE);
    }

    public int getGeneratedOutput(Location l, SlimefunBlockData data) {
        if (!SuperMultiBlockManager.canTick(l)) return 0;
        World world = l.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            return 0;
        } else {
            boolean isDaytime = isDaytime(world);

            if (!isDaytime && getNightEnergy() < 1) {
                return 0;
            } else if (!world.isChunkLoaded(l.getBlockX() >> 4, l.getBlockZ() >> 4)
                    || l.getBlock().getRelative(0, 1, 0).getLightFromSky() < (byte) lightLevel) {
                return 0;
            } else {
                return isDaytime ? getDayEnergy() : getNightEnergy();
            }
        }
    }

    private boolean isDaytime(World world) {
        long time = world.getTime();
        return !world.hasStorm() && !world.isThundering() && (time < 12300L || time > 23850L);
    }
}
