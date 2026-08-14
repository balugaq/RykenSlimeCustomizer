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
package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.simple_machine;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.AutoDrier;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.CarbonPress;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ChargingBench;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricDustWasher;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricFurnace;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricGoldPan;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricIngotFactory;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricIngotPulverizer;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricOreGrinder;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricPress;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricSmeltery;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectrifiedCrucible;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.FoodFabricator;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.Freezer;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.HeatedPressureChamber;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.Refinery;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoDisenchanter;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.AutoEnchanter;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.enchanting.BookBinder;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.BaseRSCItemGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;

public class SimpleMachineFactory {
    public static SlimefunItem create(
            YamlReader.BaseResult base,
            SimpleMachineType machineType,
            int capacity,
            int consumption,
            int speed,
            int radius,
            int repairFactor) {
        var group = base.itemGroup();
        var slimefunItemStack = base.sfis();
        var recipeType = base.recipeType();
        var recipe = base.recipe();
        SlimefunItem instance =
                switch (machineType) {
                    case ELECTRIC_FURNACE -> new ElectricFurnace(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case ELECTRIC_GOLD_PAN -> new ElectricGoldPan(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case ELECTRIC_SMELTERY -> new ElectricSmeltery(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case ELECTRIC_DUST_WASHER -> new ElectricDustWasher(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case ELECTRIC_ORE_GRINDER -> new ElectricOreGrinder(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case ELECTRIC_INGOT_FACTORY ->
                        new ElectricIngotFactory(group, slimefunItemStack, recipeType, recipe) {
                            @Override
                            public void load() {
                                SimpleMachineFactory.load(this);
                            }
                        };
                    case ELECTRIC_INGOT_PULVERIZER ->
                        new ElectricIngotPulverizer(group, slimefunItemStack, recipeType, recipe) {
                            @Override
                            public void load() {
                                SimpleMachineFactory.load(this);
                            }
                        };
                    case CHARGING_BENCH -> new ChargingBench(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case FREEZER -> new Freezer(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case CARBON_PRESS -> new CarbonPress(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case ELECTRIC_PRESS -> new ElectricPress(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case ELECTRIC_CRUCIBLE -> new ElectrifiedCrucible(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case FOOD_FABRICATOR -> new FoodFabricator(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case HEATED_PRESSURE_CHAMBER ->
                        new HeatedPressureChamber(group, slimefunItemStack, recipeType, recipe) {
                            @Override
                            public void load() {
                                SimpleMachineFactory.load(this);
                            }
                        };
                    case BOOK_BINDER -> new BookBinder(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case AUTO_ENCHANTER -> new AutoEnchanter(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case AUTO_DISENCHANTER -> new AutoDisenchanter(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case AUTO_DRIER -> new AutoDrier(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
                    case AUTO_BREWER -> new AdvancedAutoBrewer(group, slimefunItemStack, recipeType, recipe, speed);
                    case REFINERY -> new Refinery(group, slimefunItemStack, recipeType, recipe) {
                        @Override
                        public void load() {
                            SimpleMachineFactory.load(this);
                        }
                    };
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

    public static void load(SlimefunItem sf) {
        if (!sf.isHidden()) {
            BaseRSCItemGroup.addItemToGroup(sf.getItemGroup(), sf);
        }

        sf.getRecipeType().register(sf.getRecipe(), sf.getRecipeOutput());
    }
}
