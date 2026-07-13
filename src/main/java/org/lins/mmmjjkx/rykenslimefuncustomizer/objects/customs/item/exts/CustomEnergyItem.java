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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.item.exts;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.ToolUseHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.WeaponUseHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.parent.CustomItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;

public class CustomEnergyItem extends CustomItem implements Rechargeable, NotPlaceable {
    private final float capacity;

    private final Object[] constructorArgs;

    public CustomEnergyItem(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            float capacity,
            @Nullable ScriptEval eval,
            ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);

        this.capacity = capacity;

        if (eval != null) {
            eval.doInit();

            this.addItemHandler((ItemUseHandler) e -> {
                eval.evalFunction("onUse", e, this);
                e.cancel();
            });

            this.addItemHandler((WeaponUseHandler) (e, p, it) -> {
                eval.evalFunction("onWeaponHit", e, p, it);
            });
            this.addItemHandler((ToolUseHandler) (e, it, i, drops) -> eval.evalFunction("onToolUse", e, it, i, drops));
        } else {
            this.addItemHandler((ItemUseHandler) PlayerRightClickEvent::cancel);
        }

        this.constructorArgs = new Object[] {itemGroup, item, recipeType, recipe, capacity, eval, recipeOutput};
    }

    public void setItemCharge(ItemStack item, int charge) {
        Rechargeable.super.setItemCharge(item, charge);
    }

    public void setItemCharge(ItemStack item, double charge) {
        Rechargeable.super.setItemCharge(item, (float) charge);
    }

    public void addItemCharge(ItemStack item, int charge) {
        Rechargeable.super.addItemCharge(item, charge);
    }

    public void addItemCharge(ItemStack item, double charge) {
        Rechargeable.super.addItemCharge(item, (float) charge);
    }

    public void removeItemCharge(ItemStack item, int charge) {
        Rechargeable.super.removeItemCharge(item, charge);
    }

    public void removeItemCharge(ItemStack item, double charge) {
        Rechargeable.super.addItemCharge(item, (float) charge);
    }

    public float getItemCharge(ItemStack item) {
        return Rechargeable.super.getItemCharge(item);
    }

    @Override
    public float getMaxItemCharge(ItemStack itemStack) {
        return capacity;
    }

    @Override
    public Object[] constructorArgs() {
        return constructorArgs;
    }
}
