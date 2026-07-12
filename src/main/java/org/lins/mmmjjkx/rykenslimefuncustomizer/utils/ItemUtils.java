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
package org.lins.mmmjjkx.rykenslimefuncustomizer.utils;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {
    /**
     * @param itemStacks original item array
     * @return how many items there are in total
     */
    public static int getAllItemAmount(@Nonnull ItemStack... itemStacks) {
        int amount = 0;
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }

            amount += itemStack.getAmount();
        }

        return amount;
    }

    /**
     * @param itemStacks original item array
     * @return how many kinds of item there are in total
     */
    public static int getAllItemTypeAmount(@Nonnull ItemStack... itemStacks) {
        Set<SlimefunItem> sfItems = new HashSet<>();
        Set<Material> materials = new HashSet<>();

        for (ItemStack itemStack : itemStacks) {

            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }

            SlimefunItem sfItem = SlimefunItem.getByItem(itemStack);
            if (sfItem != null) {
                sfItems.add(sfItem);
            } else {
                materials.add(itemStack.getType());
            }
        }

        return sfItems.size() + materials.size();
    }
}
