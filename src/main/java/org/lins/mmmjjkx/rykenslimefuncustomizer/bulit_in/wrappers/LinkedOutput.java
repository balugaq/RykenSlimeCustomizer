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
package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CustomMachineRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record LinkedOutput(
        ItemStack[] freeOutput,
        List<ItemWrapper> freeOutputWrappers,
        Map<Integer, ItemStack> linkedOutput,
        IntList freeChance,
        Map<Integer, Integer> linkedChances) {

    public int size() {
        return freeOutput.length + linkedOutput.size();
    }

    public ItemStack[] toArray() {
        ItemStack[] result = new ItemStack[freeOutput.length + linkedOutput.size()];
        System.arraycopy(freeOutput, 0, result, 0, freeOutput.length);
        int i = freeOutput.length;
        for (ItemStack item : linkedOutput.values()) {
            result[i] = item;
            i++;
        }

        return result;
    }

    public IntList chancesToList() {
        IntList result = new IntArrayList(freeChance.size() + linkedChances.size());
        for (int chance : freeChance) {
            result.add(chance);
        }
        result.addAll(linkedChances.values());
        return result;
    }

    public Map<Integer, ItemStack> getLinkedMatchChanceResult(boolean chooseOne) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();

        for (var e : linkedOutput.entrySet()) {
            int slot = e.getKey();
            if (CustomMachineRecipe.matchChance(linkedChances().get(slot))) {
                itemStacks.put(slot, e.getValue());
                if (chooseOne) return itemStacks;
            }
        }

        return itemStacks;
    }

    public List<ItemStack> getFreeMatchChanceResult(boolean chooseOne) {
        List<ItemStack> itemStacks = new ArrayList<>();

        for (int i = 0; i < freeOutput().length; i++) {
            if (CustomMachineRecipe.matchChance(freeChance().getInt(i))) {
                itemStacks.add(freeOutput()[i]);
                if (chooseOne) return itemStacks;
            }
        }

        return itemStacks;
    }
}
