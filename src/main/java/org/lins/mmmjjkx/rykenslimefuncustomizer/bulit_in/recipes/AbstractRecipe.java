package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractRecipe extends MachineRecipe implements Recipe {
    private int ticks;
    public AbstractRecipe(int seconds, ItemStack[] input, ItemStack[] output) {
        super(seconds, input, output);
    }

    public AbstractRecipe(ItemStack[] input, ItemStack[] output, int ticks) {
        super(ticks / 2, input, output);
        this.ticks = ticks;
    }

    @Override
    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public int getTicks() {
        return ticks;
    }
}
