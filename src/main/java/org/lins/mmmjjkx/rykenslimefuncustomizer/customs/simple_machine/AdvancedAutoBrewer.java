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
package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.simple_machine;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.AutoBrewer;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.BaseRSCItemGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.super_multiblock.SuperMultiBlockManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ReflectionUtil;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class AdvancedAutoBrewer extends AutoBrewer {
    private static final Map<Material, PotionType> potionRecipes = new EnumMap<>(Material.class);
    private static final Map<PotionType, PotionType> fermentations = new EnumMap<>(PotionType.class);
    private final int speed;

    public AdvancedAutoBrewer(
        ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int speed) {
        super(itemGroup, item, recipeType, recipe);

        this.speed = speed;
    }

    @Override
    public void load() {
        if (!hidden) {
            BaseRSCItemGroup.addItemToGroup(getItemGroup(), this);
        }

        getRecipeType().register(getRecipe(), getRecipeOutput());
    }

    public boolean isPotion0(Material mt) {
        return (boolean) ReflectionUtil.invokeMethod(this, "isPotion", mt);
    }

    public ItemStack brew0(Material mt, Material mt2, PotionMeta meta) {
        return (ItemStack) ReflectionUtil.invokeMethod(this, "brew", mt, mt2, meta);
    }

    @Nullable
    protected MachineRecipe findNextRecipe(BlockMenu menu) {
        if (!SuperMultiBlockManager.canTick(menu.getLocation())) return null;

        ItemStack input1 = menu.getItemInSlot(this.getInputSlots()[0]);
        ItemStack input2 = menu.getItemInSlot(this.getInputSlots()[1]);
        if (input1 != null && input2 != null) {
            if (!this.isPotion0(input1.getType()) && !this.isPotion0(input2.getType())) {
                return null;
            } else {
                boolean isPotionInFirstSlot = this.isPotion0(input1.getType());
                ItemStack ingredient = isPotionInFirstSlot ? input2 : input1;
                if (ingredient.hasItemMeta()) {
                    return null;
                } else {
                    ItemStack potionItem = isPotionInFirstSlot ? input1 : input2;
                    PotionMeta potion = (PotionMeta) potionItem.getItemMeta();
                    ItemStack output = this.brew0(ingredient.getType(), potionItem.getType(), potion);
                    if (output == null) {
                        return null;
                    } else {
                        output.setItemMeta(potion);
                        if (!InvUtils.fits(menu.toInventory(), output, this.getOutputSlots())) {
                            return null;
                        } else {
                            for (int slot : this.getInputSlots()) {
                                menu.consumeItem(slot);
                            }

                            return new MachineRecipe(
                                30 / speed, new ItemStack[]{input1, input2}, new ItemStack[]{output});
                        }
                    }
                }
            }
        } else {
            return null;
        }
    }
}
