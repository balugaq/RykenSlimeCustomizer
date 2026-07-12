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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.inventory.ItemStack;

@Getter
public class CustomMachineRecipe extends MachineRecipe {
    private final List<Integer> chances;
    private final IntList noConsume;

    private final boolean chooseOneIfHas;
    private final boolean forDisplay;
    private final boolean hide;

    public CustomMachineRecipe(
            int seconds,
            ItemStack[] input,
            ItemStack[] output,
            List<Integer> chances,
            boolean chooseOneIfHas,
            boolean forDisplay,
            boolean hide,
            IntList noConsumeIndexes) {
        super(seconds, input.clone(), output.clone());

        this.chances = chances;
        this.chooseOneIfHas = chooseOneIfHas;
        this.forDisplay = forDisplay;
        this.hide = hide;
        this.noConsume = noConsumeIndexes;
    }

    public List<ItemStack> getMatchChanceResult() {
        List<ItemStack> itemStacks = new ArrayList<>();

        for (int i = 0; i < getOutput().length; i++) {
            ItemStack output = getOutput()[i];
            int chance = chances.get(i);
            if (matchChance(chance)) {
                itemStacks.add(output);
            }
        }

        return itemStacks;
    }

    private boolean matchChance(Integer chance) {
        if (chance == null) return false;
        if (chance >= 100) return true;
        if (chance < 1) return false;

        int result = new SecureRandom().nextInt(100);
        return result < chance;
    }
}
