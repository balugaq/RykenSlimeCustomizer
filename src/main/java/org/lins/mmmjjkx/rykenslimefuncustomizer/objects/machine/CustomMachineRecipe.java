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

import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.AbstractRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.InvIndex;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.ItemWrapper;
import org.lins.mmmjjkx.rykenslimefuncustomizer.listeners.SingleItemRecipeGuideListener;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.BlockMenuUtil;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.StackUtils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.RecipesHolder.RECIPE_INPUT;
import static org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.RecipesHolder.RECIPE_OUTPUT;

@NullMarked
@Getter
public class CustomMachineRecipe extends AbstractRecipe {
    private final IntList chances;
    private final IntList noConsume;

    private final List<ItemWrapper> inputs;
    private final List<ItemWrapper> outputs;
    private final boolean chooseOne;
    private final boolean forDisplayOnly;
    private final boolean hide;

    @Deprecated
    public boolean isChooseOneIfHas() {
        return chooseOne;
    }

    @Deprecated
    public boolean isForDisplay() {
        return forDisplayOnly;
    }

    public CustomMachineRecipe(
            int seconds,
            ItemStack[] input,
            ItemStack[] output,
            IntList chances,
            boolean chooseOne,
            boolean forDisplayOnly,
            boolean hide,
            IntList noConsumeIndexes) {
        this(input, output, seconds * 2, chances, chooseOne, forDisplayOnly, hide, noConsumeIndexes);
    }

    public CustomMachineRecipe(
        ItemStack[] input,
        ItemStack[] output,
        int ticks,
        IntList chances,
        boolean chooseOne,
        boolean forDisplayOnly,
        boolean hide,
        IntList noConsumeIndexes) {
        super(input, output, ticks);

        this.inputs = InvIndex.mergeItems(input);
        this.outputs = InvIndex.mergeItems(output);
        this.chances = chances;
        this.chooseOne = chooseOne;
        this.forDisplayOnly = forDisplayOnly;
        this.hide = hide;
        this.noConsume = noConsumeIndexes;
    }

    public List<ItemWrapper> getMatchChanceResult(boolean chooseOne) {
        List<ItemWrapper> wrappers = new ArrayList<>();

        for (int i = 0; i < getOutputs().size(); i++) {
            if (matchChance(chances.getInt(i))) {
                wrappers.add(getOutputs().get(i).clone());
            }
        }

        if (chooseOne) {
            int index = new Random().nextInt(wrappers.size());
            return List.of(wrappers.get(index));
        }

        return wrappers;
    }

    public static boolean matchChance(int chance) {
        if (chance >= 100) return true;
        if (chance < 1) return false;

        int result = new SecureRandom().nextInt(100);
        return result < chance;
    }

    @Override
    public boolean matches(InvIndex index, boolean consumeItems) {
        if (this.isForDisplayOnly()) return false;

        for (var input : inputs) {
            if (!index.matches(input)) {
                return false;
            }
        }

        int slotsOutputTakes = outputs.stream().map(ItemWrapper::countStack).mapToInt(i -> i).sum();
        if (index.getEmptyOutputSlots() < slotsOutputTakes) { // fast check
            if (!BlockMenuUtil.fits(index.getInv(), outputs, index.getInv().getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW))) {
                // cannot output items
                return false;
            }
        }

        if (consumeItems) {
            var inv = index.getInv();
            for (var wrapper : inputs) {
                for (var slotWrapper : index.getInputs()) {
                    if (StackUtils.itemsMatch(slotWrapper.getStack(), wrapper.getStack())) {
                        int left = wrapper.getAmount();
                        for (var entry : slotWrapper.getAmounts().int2IntEntrySet()) {
                            int slot = entry.getIntKey();
                            int curr = entry.getIntValue();
                            if (curr <= left) {
                                left -= curr;
                                inv.consumeItem(slot, curr);
                            } else {
                                inv.consumeItem(slot, left);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean pushOutputs(BlockMenu inv) {
        BlockMenuUtil.pushItems(inv, outputs, inv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW));
        return true;
    }

    @Override
    public ItemStack getDisplayInput(int index) {
        if (inputs.size() == 1) return inputs.getFirst().getStack();
        return SingleItemRecipeGuideListener.tagItem(RECIPE_INPUT, index);
    }

    @Override
    public ItemStack getDisplayOutput(int index) {
        if (outputs.size() == 1) {
            ItemStack out = outputs.getFirst().getStack().clone();
            CommonUtils.addLore(out, true, CommonUtils.richFormatSeconds(getTicks() / 2));
            return out;
        } else {
            return SingleItemRecipeGuideListener.tagItem(RECIPE_OUTPUT, index);
        }
    }
}
