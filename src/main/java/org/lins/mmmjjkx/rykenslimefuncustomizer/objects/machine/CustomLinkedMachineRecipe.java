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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.InvIndex;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.LinkedOutput;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.BlockMenuUtil;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.StackUtils;

import java.util.Map;
import java.util.Set;

@NullMarked
@Getter
public class CustomLinkedMachineRecipe extends CustomMachineRecipe {
    private final Set<Integer> noConsumes;
    private final Map<Integer, ItemStack> linkedInput;
    private final LinkedOutput linkedOutput;
    private final int saveAmount;

    @Override
    public boolean pushOutputs(BlockMenu inv) {
        var slots = inv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW);
        return BlockMenuUtil.pushItem(inv, linkedOutput, false, slots).isEmpty();
    }

    @Override
    public boolean matches(InvIndex index, boolean consumeItems) {
        for (var e : linkedInput.entrySet()) {
            ItemStack stack = index.getItemInSlot(e.getKey());
            if (stack == null
                || stack.getAmount() < e.getValue().getAmount() + saveAmount
                || !StackUtils.itemsMatch(e.getValue(), stack, true, false)) {
                return false;
            }
        }

        if (!BlockMenuUtil.fits(index.getInv(), linkedOutput)) return false;

        if (consumeItems) {
            for (var e : linkedInput.entrySet()) {
                ItemStack stack = index.getItemInSlot(e.getKey());
                if (stack != null) {
                    stack.setAmount(stack.getAmount() - e.getValue().getAmount());
                }
            }
        }
        return true;
    }

    public CustomLinkedMachineRecipe(
            int seconds,
            Map<Integer, ItemStack> input,
            LinkedOutput linkedOutput,
            boolean chooseOneIfHas,
            boolean forDisplay,
            boolean hide,
            Set<Integer> noConsumes,
            int saveAmount) {
        super(
                seconds,
                input.values().toArray(new ItemStack[0]),
                linkedOutput.toArray(),
                linkedOutput.chancesToList(),
                chooseOneIfHas,
                forDisplay,
                hide,
                new IntArrayList());
        this.linkedInput = input;
        this.linkedOutput = linkedOutput;
        this.noConsumes = noConsumes;
        this.saveAmount = saveAmount;
    }
}
