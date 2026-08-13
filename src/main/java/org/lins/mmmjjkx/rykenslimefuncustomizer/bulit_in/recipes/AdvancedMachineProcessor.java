package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import io.github.thebusybiscuit.slimefun4.api.events.AsyncMachineOperationFinishEvent;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@NullMarked
@Getter
public class AdvancedMachineProcessor extends MachineProcessor<CraftingOperation> {
    private final Map<BlockPosition, CraftingRecipeOperation> operations = new HashMap<>();

    @Override
    public CraftingRecipeOperation getOperation(Location loc) {
        return operations.get(new BlockPosition(loc));
    }

    @Deprecated(forRemoval = true)
    @Override
    public boolean startOperation(Location loc, CraftingOperation operation) {
        return false;
    }

    @Deprecated(forRemoval = true)
    @Override
    public boolean startOperation(Block b, CraftingOperation operation) {
        return false;
    }

    @Deprecated(forRemoval = true)
    @Override
    public boolean startOperation(BlockPosition pos, CraftingOperation operation) {
        return false;
    }

    public boolean startOperation(Location loc, CraftingRecipeOperation operation) {
        return startOperation(new BlockPosition(loc), operation);
    }

    public boolean startOperation(Block b, CraftingRecipeOperation operation) {
        return startOperation(b.getLocation(), operation);
    }

    public boolean startOperation(BlockPosition pos, CraftingRecipeOperation operation) {
        return operations.putIfAbsent(pos, operation) == null;
    }

    @Override
    public @Nullable CraftingRecipeOperation getOperation(Block b) {
        return getOperation(b.getLocation());
    }

    @Override
    public @Nullable CraftingRecipeOperation getOperation(BlockPosition pos) {
        return operations.get(pos);
    }

    @Override
    public boolean endOperation(Location loc) {
        return endOperation(new BlockPosition(loc));
    }

    @Override
    public boolean endOperation(Block b) {
        return endOperation(b.getLocation());
    }

    @Override
    public boolean endOperation(BlockPosition pos) {
        CraftingRecipeOperation operation = operations.remove(pos);

        if (operation != null) {
            /*
             * Only call an event if the operation actually finished.
             * If it was ended prematurely (aka aborted), then we don't call any event.
             */
            if (operation.isFinished()) {
                Event event = new AsyncMachineOperationFinishEvent(pos, this, operation);
                Bukkit.getPluginManager().callEvent(event);
            } else {
                operation.onCancel(pos);
            }

            return true;
        } else {
            return false;
        }
    }

    public AdvancedMachineProcessor(MachineProcessHolder<CraftingOperation> owner) {
        super(owner);
    }
}
