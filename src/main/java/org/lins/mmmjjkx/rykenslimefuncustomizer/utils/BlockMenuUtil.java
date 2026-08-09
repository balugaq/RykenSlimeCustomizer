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

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.ItemWrapper;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.LinkedOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class BlockMenuUtil {
    @Nullable public static ItemStack pushItem(@Nonnull BlockMenu blockMenu, @Nonnull ItemStack item, int... slots) {
        if (item == null || item.getType().isAir()) {
            throw new IllegalArgumentException("Cannot push null or AIR");
        }

        int leftAmount = item.getAmount();

        for (int slot : slots) {
            if (leftAmount <= 0) {
                break;
            }

            ItemStack existing = blockMenu.getItemInSlot(slot);

            if (existing == null || existing.getType().isAir()) {
                int received = Math.min(leftAmount, item.getMaxStackSize());
                ItemStack clone = item.clone();
                clone.setAmount(received);
                blockMenu.replaceExistingItem(slot, clone);
                leftAmount -= received;
                item.setAmount(Math.max(0, leftAmount));
            } else {
                int existingAmount = existing.getAmount();
                if (existingAmount >= item.getMaxStackSize()) {
                    continue;
                }

                if (!StackUtils.itemsMatch(item, existing, true, false)) {
                    continue;
                }

                int received = Math.max(0, Math.min(item.getMaxStackSize() - existingAmount, leftAmount));
                leftAmount -= received;
                existing.setAmount(existingAmount + received);
                item.setAmount(leftAmount);
            }
        }

        if (leftAmount > 0) {
            return new CustomItemStack(item, leftAmount);
        } else {
            return null;
        }
    }

    @Nonnull
    public static Map<ItemStack, Integer> pushItem(
            @Nonnull BlockMenu blockMenu, @Nonnull ItemStack[] items, int... slots) {
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("Cannot push null or empty array");
        }

        List<ItemStack> listItems = new ArrayList<>();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                listItems.add(item);
            }
        }

        return pushItem(blockMenu, listItems, slots);
    }

    @Nonnull
    public static Map<ItemStack, Integer> pushItem(
            @Nonnull BlockMenu blockMenu, @Nonnull List<ItemStack> items, int... slots) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot push null or empty list");
        }

        Map<ItemStack, Integer> itemMap = new HashMap<>();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                ItemStack leftOver = pushItem(blockMenu, item, slots);
                if (leftOver != null) {
                    itemMap.put(leftOver, itemMap.getOrDefault(leftOver, 0) + leftOver.getAmount());
                }
            }
        }

        return itemMap;
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull ItemWrapper wrapper, int... slots) {
        if (wrapper == null || wrapper.getType() == Material.AIR) {
            return true;
        }

        int incoming = wrapper.getAmount();
        for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);

            if (stack == null || stack.getType() == Material.AIR) {
                incoming -= wrapper.getMaxStackSize();
            } else if (stack.getMaxStackSize() > stack.getAmount() && StackUtils.itemsMatch(wrapper.getStack(), stack, true, false)) {
                incoming -= stack.getMaxStackSize() - stack.getAmount();
            }

            if (incoming <= 0) {
                return true;
            }
        }

        return false;
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull List<ItemWrapper> wrappers, int... slots) {
        int emptySlots = 0;
        for (int slot : slots) {
            ItemStack stack = blockMenu.getItemInSlot(slot);
            if (stack == null || stack.getType() == Material.AIR) emptySlots++;
        }

        for (ItemWrapper wrapper : wrappers) {
            int incoming = wrapper.getAmount();
            // find existing first
            for (int slot : slots) {
                ItemStack stack = blockMenu.getItemInSlot(slot);

                if (stack.getMaxStackSize() > stack.getAmount() && StackUtils.itemsMatch(wrapper.getStack(), stack, true, false)) {
                    incoming -= stack.getMaxStackSize() - stack.getAmount();
                }

                if (incoming <= 0) break;
            }

            emptySlots -= wrapper.countStack(incoming);
            if (emptySlots < 0) return false;
        }

        return true;
    }

    public static boolean fits(@Nonnull BlockMenu blockMenu, @Nonnull LinkedOutput output, int... slots) {
        int emptySlots = 0;
        for (int slot : slots) {
            if (output.linkedOutput().containsKey(slot)) continue;
            ItemStack stack = blockMenu.getItemInSlot(slot);
            if (stack == null || stack.getType() == Material.AIR) emptySlots++;
        }

        Int2ObjectOpenHashMap<ItemStack> linked = new Int2ObjectOpenHashMap<>();
        for (var e : output.linkedOutput().entrySet()) {
            int slot = e.getKey();
            var item = e.getValue();
            var exist = blockMenu.getItemInSlot(slot);
            var ap = exist.getAmount() + item.getAmount();
            if (!StackUtils.itemsMatch(item, exist, true, false)
                || ap > item.getMaxStackSize()) {
                return false;
            }
            linked.put(slot, item.asQuantity(ap));
        }


        // try to push free output
        for (ItemWrapper wrapper : output.freeOutputWrappers()) {
            int incoming = wrapper.getAmount();
            // find existing first
            for (int slot : slots) {
                ItemStack stack = linked.containsKey(slot) ? linked.get(slot) : blockMenu.getItemInSlot(slot);

                if (stack.getMaxStackSize() > stack.getAmount() && StackUtils.itemsMatch(wrapper.getStack(), stack, true, false)) {
                    incoming -= stack.getMaxStackSize() - stack.getAmount();
                }

                if (incoming <= 0) break;
            }

            emptySlots -= wrapper.countStack(incoming);
            if (emptySlots < 0) return false;
        }

        // all items should be pushed successfully
        return true;
    }

    public static List<ItemStack> pushItem(
            @Nonnull BlockMenu blockMenu, @Nonnull LinkedOutput output, boolean chooseOne, int... slots) {
        List<ItemStack> failed = new ArrayList<>();
        // push linked output
        var result = output.getLinkedMatchChanceResult(chooseOne);
        for (var e : result.entrySet()) {
            // ignore if not enough space
            var r = pushItem(blockMenu, e.getValue().clone(), e.getKey());
            if (r != null) failed.add(r);
        }
        if (!result.isEmpty() && chooseOne) return failed;

        // push free output
        var result2 = output.getFreeMatchChanceResult(chooseOne);
        for (var item : result2) {
            var r = pushItem(blockMenu, item.clone(), slots);
            if (r != null) failed.add(r);
        }

        return failed;
    }
}
