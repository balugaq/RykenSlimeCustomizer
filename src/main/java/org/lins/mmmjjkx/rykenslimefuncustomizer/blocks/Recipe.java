package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import it.unimi.dsi.fastutil.ints.IntList;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Recipe {
    default boolean matches(InvIndex index) {
        return matches(index, true);
    }

    default boolean isChooseOne() {
        return false;
    }
    default boolean isForDisplayOnly() {
        return false;
    }
    default boolean isHide() {
        return false;
    }

    boolean matches(InvIndex index, boolean consumeItems);
    int getTicks();
    boolean pushOutputs(BlockMenu inv);
    IntList getChances();
    IntList getNoConsume();

    ItemStack getDisplayInput(int index);
    ItemStack getDisplayOutput(int index);
}
