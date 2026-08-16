package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers;

import lombok.Data;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.StackUtils;

import java.util.ArrayList;
import java.util.List;

@NullMarked
@Data
public class InvIndex {
    private final List<SlotWrapper> inputs;
    private final List<SlotWrapper> outputs;
    private final BlockMenu inv;
    private final int emptyOutputSlots;
    private InvIndex(List<SlotWrapper> inputs, List<SlotWrapper> outputs, BlockMenu inv, int emptyOutputSlots) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.inv = inv;
        this.emptyOutputSlots = emptyOutputSlots;
    }

    public static void addToWrappers(ItemStack stack, int slot, List<SlotWrapper> wrappers) {
        boolean matched = false;
        for (SlotWrapper wrapper : wrappers) {
            // pre-merge all items
            if (StackUtils.itemsMatch(wrapper.getStack(), stack)) {
                wrapper.addAmount(slot, stack.getAmount());
                matched = true;
                break;
            }
        }
        if (!matched) {
            wrappers.add(SlotWrapper.create(stack, slot));
        }
    }

    public static InvIndex create(BlockMenu inv) {
        List<SlotWrapper> inputs = new ArrayList<>();
        for (int slot : inv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.INSERT)) {
            ItemStack stack = inv.getItemInSlot(slot);
            if (stack == null || stack.getType().isAir()) continue;
            addToWrappers(stack, slot, inputs);
        }
        List<SlotWrapper> outputs = new ArrayList<>();
        int emptyOutputSlots = 0;
        for (int slot : inv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW)) {
            ItemStack stack = inv.getItemInSlot(slot);
            if (stack == null || stack.getType().isAir()) {
                emptyOutputSlots++;
                continue;
            }
            addToWrappers(stack, slot, outputs);
        }
        return new InvIndex(inputs, outputs, inv, emptyOutputSlots);
    }

    public static List<ItemWrapper> mergeItems(List<@Nullable ItemStack> itemStacks) {
        return mergeItems(itemStacks.toArray(new ItemStack[0]));
    }

    public static List<ItemWrapper> mergeItems(@Nullable ItemStack[] itemStacks) {
        List<ItemWrapper> items = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;
            boolean matched = false;
            for (ItemWrapper wrapper : items) {
                // pre-merge all items
                if (StackUtils.itemsMatch(wrapper.getStack(), itemStack)) {
                    wrapper.addAmount(itemStack.getAmount());
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                items.add(ItemWrapper.create(itemStack));
            }
        }
        return items;
    }

    public boolean contains(ItemStack item) {
        for (SlotWrapper wrapper : inputs) {
            if (StackUtils.itemsMatch(wrapper.getStack(), item)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(InputWrapper wrapper) {
        return getConsumableAmount(wrapper) >= wrapper.getConsumeAmount();
    }

    public int getConsumableAmount(InputWrapper wrapper) {
        for (SlotWrapper wp : inputs) {
            if (StackUtils.itemsMatch(wrapper.getStack(), wp.getStack())) {
                return wp.getConsumableAmount(wrapper.getNoConsume());
            }
        }
        return -100;
    }

    @Nullable
    public SlotWrapper getWrapperForSlot(int slot) {
        for (SlotWrapper wrapper : inputs) {
            if (wrapper.getAmounts().containsKey(slot)) {
                return wrapper;
            }
        }
        return null;
    }

    @Nullable
    public ItemStack getItemInSlot(int slot) {
        return inv.getItemInSlot(slot);
    }
}
