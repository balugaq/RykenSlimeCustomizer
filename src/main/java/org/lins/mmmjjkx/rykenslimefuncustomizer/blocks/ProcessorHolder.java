package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public interface ProcessorHolder extends MachineProcessHolder<CraftingOperation> {
    Map<ProcessorHolder, AdvancedMachineProcessor> processors = new HashMap<>();

    // see AContainer
    @Override
    default MachineProcessor<CraftingOperation> getMachineProcessor() {
        return getAdvancedMachineProcessor();
    }

    default AdvancedMachineProcessor getAdvancedMachineProcessor() {
        return processors.computeIfAbsent(this, k -> new AdvancedMachineProcessor(this));
    }

    default @Nullable CraftingRecipeOperation getCurrentOperation(Location location) {
        return getAdvancedMachineProcessor().getOperation(location);
    }

    @Override
    default String getId() {
        return getSlimefunItem().getId();
    }

    SlimefunItem getSlimefunItem();
}
