package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@EqualsAndHashCode(callSuper = true)
@Data
@NullMarked
public class CraftingRecipeOperation extends CraftingOperation {
    private static final ItemStack[] PLACEHOLDER = new ItemStack[] {new ItemStack(Material.AIR)};
    private final Recipe recipe;

    public CraftingRecipeOperation(Recipe recipe) {
        this(recipe, recipe.getTicks());
    }

    public CraftingRecipeOperation(Recipe recipe, int ticks) {
        super(PLACEHOLDER, PLACEHOLDER, ticks);
        this.recipe = recipe;
    }
}
