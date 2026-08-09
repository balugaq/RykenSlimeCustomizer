package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Data
public class SlotWrapper {
    private final ItemStack stack;
    private int allAmount = 0;
    private Int2IntOpenHashMap amounts;
    private SlotWrapper(ItemStack stack, int slot) {
        this.stack = stack.asOne(); // clone
        this.amounts = new Int2IntOpenHashMap();
        addAmount(slot, stack.getAmount());
    }

    public void addAmount(int slot, int amount) {
        amounts.put(slot, amount);
    }

    public static SlotWrapper create(ItemStack stack) {
        return new SlotWrapper(stack, -1);
    }

    public static SlotWrapper create(ItemStack stack, int slot) {
        return new SlotWrapper(stack, slot);
    }

    public int getConsumableAmount(IntSet noConsumes) {
        if (noConsumes.isEmpty()) return getConsumableAmount();
        return getConsumableAmount() - noConsumes.intStream().map(amounts::get).sum();
    }

    public int getConsumableAmount() {
        return allAmount;
    }
}
