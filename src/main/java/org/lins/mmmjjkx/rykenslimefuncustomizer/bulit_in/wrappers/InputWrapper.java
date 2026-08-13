package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

@NullMarked
@Getter
public class InputWrapper extends ItemWrapper {
    private final NoConsume noConsume = new NoConsume();
    private final IntSet slots = new IntOpenHashSet();

    public InputWrapper(ItemStack stack, int amount) {
        super(stack, amount);
    }

    public InputWrapper(ItemStack stack) {
        super(stack);
    }

    public static InputWrapper create(InputDesc desc) {
        var wrapper = new InputWrapper(desc.itemStack());
        wrapper.merge(desc);
        return wrapper;
    }

    public void addSlot(int slot) {
        slots.add(slot);
    }

    public void merge(InputDesc desc) {
        addAmount(desc.itemStack().getAmount());
        if (desc.noConsume()) {
            if (desc.slot() != -1) {
                noConsume.addLinkedNoConsume(desc.slot());
            } else {
                noConsume.addNoConsumeAmountExcludeLinked(desc.itemStack().getAmount());
            }
        }
    }

    public Stream<ItemStack> asArrayStream() {
        int amt = getAmount() - noConsume.getNoConsumeAmountExcludeLinked();
        return toStacks(amt).stream();
    }
}
