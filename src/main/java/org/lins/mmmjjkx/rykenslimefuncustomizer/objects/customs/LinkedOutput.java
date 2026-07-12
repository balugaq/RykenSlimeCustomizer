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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public record LinkedOutput(
        ItemStack[] freeOutput,
        Map<Integer, ItemStack> linkedOutput,
        int[] freeChances,
        Map<Integer, Integer> linkedChances) {

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

    public List<Integer> chancesToArray() {
        List<Integer> result = new ArrayList<>(freeChances.length + linkedChances.size());
        for (int chance : freeChances) {
            result.add(chance);
        }
        result.addAll(linkedChances.values());
        return result;
    }
}
