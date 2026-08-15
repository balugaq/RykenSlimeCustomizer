package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public interface ProcessorHolder extends MachineProcessHolder<CraftingOperation> {
    Map<ProcessorHolder, AdvancedMachineProcessor> processors = new ConcurrentHashMap<>();

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
        return getMachine().getId();
    }

    SlimefunItem getMachine();
}
