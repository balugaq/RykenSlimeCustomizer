package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    public Stream<ItemStack> toStackStream() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < countStack() - 1; i++) {
            list.add(stack.asQuantity(stack.getMaxStackSize()));
        }
        int left = amount - (stack.getMaxStackSize() * (countStack() - 1));
        if (left > 0) {
            list.add(stack.asQuantity(left));
        }
        return list.stream();
    }

    @Override
    public ItemWrapper clone() {
        return new ItemWrapper(stack.clone(), amount);
    }
}
