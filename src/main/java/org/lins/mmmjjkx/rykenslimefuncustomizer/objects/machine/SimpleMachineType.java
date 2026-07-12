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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine;

import lombok.Getter;

@Getter
public enum SimpleMachineType {
    ELECTRIC_SMELTERY(true),
    ELECTRIC_FURNACE(true),
    ELECTRIC_GOLD_PAN(true),
    ELECTRIC_DUST_WASHER(true),
    ELECTRIC_ORE_GRINDER(true),
    ELECTRIC_INGOT_FACTORY(true),
    ELECTRIC_INGOT_PULVERIZER(true),
    CHARGING_BENCH(true),
    ANIMAL_GROWTH_ACCELERATOR(true),
    TREE_GROWTH_ACCELERATOR(true),
    CROP_GROWTH_ACCELERATOR(true),
    FREEZER(true),
    CARBON_PRESS(true),
    ELECTRIC_PRESS(true),
    ELECTRIC_CRUCIBLE(true),
    FOOD_FABRICATOR(true),
    HEATED_PRESSURE_CHAMBER(true),
    AUTO_ENCHANTER(true),
    AUTO_DISENCHANTER(true),
    BOOK_BINDER(true),
    AUTO_ANVIL(true),
    AUTO_DRIER(true),
    AUTO_BREWER(true),
    REFINERY(true),
    PRODUCE_COLLECTOR(true);

    private final boolean energy;

    SimpleMachineType(boolean energy) {
        this.energy = energy;
    }
}
