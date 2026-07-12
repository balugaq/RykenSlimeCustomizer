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
package org.lins.mmmjjkx.rykenslimefuncustomizer.factories;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.*;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoDisenchanter;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoEnchanter;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.BookBinder;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.sf.*;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.SimpleMachineType;

public class SimpleMachineFactory {
    public static SlimefunItem create(
            ItemGroup group,
            SlimefunItemStack slimefunItemStack,
            RecipeType recipeType,
            ItemStack[] recipe,
            SimpleMachineType machineType,
            int capacity,
            int consumption,
            int speed,
            int radius,
            int repairFactor) {
        SlimefunItem instance =
                switch (machineType) {
                    case ELECTRIC_FURNACE -> new ElectricFurnace(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_GOLD_PAN -> new ElectricGoldPan(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_SMELTERY -> new ElectricSmeltery(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_DUST_WASHER -> new ElectricDustWasher(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_ORE_GRINDER -> new ElectricOreGrinder(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_INGOT_FACTORY ->
                        new ElectricIngotFactory(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_INGOT_PULVERIZER ->
                        new ElectricIngotPulverizer(group, slimefunItemStack, recipeType, recipe);
                    case CHARGING_BENCH -> new ChargingBench(group, slimefunItemStack, recipeType, recipe);
                    case FREEZER -> new Freezer(group, slimefunItemStack, recipeType, recipe);
                    case CARBON_PRESS -> new CarbonPress(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_PRESS -> new ElectricPress(group, slimefunItemStack, recipeType, recipe);
                    case ELECTRIC_CRUCIBLE -> new ElectrifiedCrucible(group, slimefunItemStack, recipeType, recipe);
                    case FOOD_FABRICATOR -> new FoodFabricator(group, slimefunItemStack, recipeType, recipe);
                    case HEATED_PRESSURE_CHAMBER ->
                        new HeatedPressureChamber(group, slimefunItemStack, recipeType, recipe);
                    case BOOK_BINDER -> new BookBinder(group, slimefunItemStack, recipeType, recipe);
                    case AUTO_ENCHANTER -> new AutoEnchanter(group, slimefunItemStack, recipeType, recipe);
                    case AUTO_DISENCHANTER -> new AutoDisenchanter(group, slimefunItemStack, recipeType, recipe);
                    case AUTO_DRIER -> new AutoDrier(group, slimefunItemStack, recipeType, recipe);
                    case AUTO_BREWER -> new AdvancedAutoBrewer(group, slimefunItemStack, recipeType, recipe, speed);
                    case REFINERY -> new Refinery(group, slimefunItemStack, recipeType, recipe);
                    case PRODUCE_COLLECTOR ->
                        new AdvancedProduceCollector(group, slimefunItemStack, recipeType, recipe, speed);
                    case TREE_GROWTH_ACCELERATOR ->
                        new AdvancedTreeGrowthAccelerator(
                                group, slimefunItemStack, recipeType, recipe, capacity, radius, consumption);
                    case ANIMAL_GROWTH_ACCELERATOR ->
                        new AdvancedAnimalGrowthAccelerator(
                                group, slimefunItemStack, recipeType, recipe, capacity, radius, consumption);
                    case CROP_GROWTH_ACCELERATOR ->
                        new AdvancedCropGrowthAccelerator(
                                group, slimefunItemStack, recipeType, recipe, capacity, radius, consumption, speed);
                    case AUTO_ANVIL ->
                        new AdvancedAutoAnvil(group, repairFactor, slimefunItemStack, recipeType, recipe, speed);
                };

        if (instance instanceof AContainer aContainer) {
            aContainer.setCapacity(capacity);
            aContainer.setEnergyConsumption(consumption);
            aContainer.setProcessingSpeed(speed);
        }

        return instance;
    }
}
