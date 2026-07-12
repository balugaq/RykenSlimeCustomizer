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

import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.Validate;
import lombok.Getter;

public class CustomTemplateCraftingOperation implements MachineOperation {
    @Getter
    private final CustomMachineRecipe recipe;

    @Getter
    private final MachineTemplate template;

    private final int ticks;
    private int currentTicks;

    public CustomTemplateCraftingOperation(MachineTemplate template, CustomMachineRecipe recipe, int totalTicks) {
        this.currentTicks = 0;
        Validate.isTrue(recipe.getOutput().length != 0, "The recipe must have at least one output.");
        Validate.isTrue(
                totalTicks >= 0,
                "The amount of total ticks must be a positive integer or zero, received: " + totalTicks);
        this.recipe = recipe;
        this.ticks = totalTicks;
        this.template = template;
    }

    public void addProgress(int num) {
        Validate.isTrue(num > 0, "Progress must be positive.");
        this.currentTicks += num;
    }

    public int getProgress() {
        return this.currentTicks;
    }

    public int getTotalTicks() {
        return ticks;
    }
}
