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
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.LinkedOutput;

@Getter
public class CustomLinkedMachineRecipe extends CustomMachineRecipe {
    private final Set<Integer> noConsumes;
    private final Map<Integer, ItemStack> linkedInput;
    private final LinkedOutput linkedOutput;

    public CustomLinkedMachineRecipe(
            int seconds,
            Map<Integer, ItemStack> input,
            LinkedOutput linkedOutput,
            boolean chooseOneIfHas,
            boolean forDisplay,
            boolean hide,
            Set<Integer> noConsumes) {
        super(
                seconds,
                input.values().toArray(new ItemStack[0]),
                linkedOutput.toArray(),
                linkedOutput.chancesToArray(),
                chooseOneIfHas,
                forDisplay,
                hide,
                new IntArrayList());
        this.linkedInput = input;
        this.linkedOutput = linkedOutput;
        this.noConsumes = noConsumes;
    }
}
