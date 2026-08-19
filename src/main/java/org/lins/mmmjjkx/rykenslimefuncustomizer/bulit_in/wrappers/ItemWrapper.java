package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
@Data
public class ItemWrapper implements Cloneable {
    private final ItemStack stack;
    private int amount;

    public ItemWrapper(ItemStack stack) {
        this(stack, stack.getAmount());
    }

    public ItemWrapper(ItemStack stack, int amount) {
        this.stack = stack.asOne(); // clone
        this.amount = amount;
    }

    public Material getType() {
        return stack.getType();
    }

    public int getMaxStackSize() {
        return stack.getMaxStackSize();
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public static ItemWrapper create(ItemStack stack) {
        return new ItemWrapper(stack);
    }

    public int countStack(int amount) {
        return (amount + stack.getMaxStackSize() - 1) / stack.getMaxStackSize();
    }

    public int countStack() {
        return countStack(amount);
    }

    public List<ItemStack> toStacks() {
        return toStacks(amount);
    }

    public List<ItemStack> toStacks(int amt) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < countStack(amt) - 1; i++) {
            list.add(stack.asQuantity(stack.getMaxStackSize()));
        }
        int left = amt - (stack.getMaxStackSize() * Math.max(0, countStack(amt) - 1));
        if (left > 0) {
            list.add(stack.asQuantity(left));
        }
        return list;
    }

    public ItemStack asOneStack() {
        return stack.asQuantity(Math.min(stack.getMaxStackSize(), getAmount()));
    }

    @Override
    public ItemWrapper clone() {
        return new ItemWrapper(stack.clone(), amount);
    }
}
