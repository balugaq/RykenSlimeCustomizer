package org.lins.mmmjjkx.rykenslimefuncustomizer.customs;

import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class CustomRecipeType extends RecipeType {
    public CustomRecipeType(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    public CustomRecipeType(NamespacedKey key, ItemStack item, BiConsumer<ItemStack[], ItemStack> registerCallback, BiConsumer<ItemStack[], ItemStack> unregisterCallback) {
        super(key, item, registerCallback, unregisterCallback);
    }
}
