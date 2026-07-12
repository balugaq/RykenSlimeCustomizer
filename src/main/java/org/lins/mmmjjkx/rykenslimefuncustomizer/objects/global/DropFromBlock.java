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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.global;

import java.util.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;

public class DropFromBlock {
    private static final Map<Material, List<Drop>> drops;

    static {
        drops = new HashMap<>();
    }

    public static void addDrop(Material material, Drop drop) {
        drops.computeIfAbsent(material, k -> new ArrayList<>()).add(drop);
    }

    public static List<Drop> getDrops(Material material) {
        return drops.getOrDefault(material, Collections.emptyList());
    }

    public static void removeDrop(Material material, Drop drop) {
        List<Drop> dropsList = getDrops(material);
        dropsList.remove(drop);
        if (dropsList.isEmpty()) {
            drops.remove(material);
        }
    }

    public static void unregisterAddonDrops(ProjectAddon addon) {
        for (Material material : new ArrayList<>(drops.keySet())) {
            List<Drop> dropsList = getDrops(material);
            dropsList.removeIf(drop -> drop.owner.equals(addon));
            if (dropsList.isEmpty()) {
                drops.remove(material);
            }
        }
    }

    public record Drop(ItemStack itemStack, int dropChance, ProjectAddon owner, int minDropAmount, int maxDropAmount) {
        public Drop(ItemStack itemStack, int dropChance, ProjectAddon owner) {
            this(itemStack, dropChance, owner, itemStack.getAmount(), itemStack.getAmount());
        }

        @Override
        public ItemStack itemStack() {
            ItemStack itemStack = this.itemStack.clone();
            itemStack.setAmount(randomDropAmount());
            return itemStack;
        }

        private int randomDropAmount() {
            Random random = new Random();
            int min = Math.min(minDropAmount, maxDropAmount);
            int max = Math.max(minDropAmount, maxDropAmount);
            return random.nextInt(max - min + 1) + min;
        }
    }
}
