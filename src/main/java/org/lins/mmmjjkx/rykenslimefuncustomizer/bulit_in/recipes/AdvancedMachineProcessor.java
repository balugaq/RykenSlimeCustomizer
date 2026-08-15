package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import io.github.thebusybiscuit.slimefun4.api.events.AsyncMachineOperationFinishEvent;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
@Getter
public class AdvancedMachineProcessor extends MachineProcessor<CraftingOperation> {
    private final Map<BlockPosition, CraftingRecipeOperation> operations = new ConcurrentHashMap<>();

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

    @Override
    public void updateProgressBar(BlockMenu inv, int slot, CraftingOperation operation) {
        if (getProgressBar() == null) {
            // No progress bar, no need to update anything.
            return;
        }

        // Update the progress bar in our inventory (if anyone is watching)
        int remainingTicks = operation.getRemainingTicks();
        int totalTicks = operation.getTotalTicks();

        // Fixes #3538 - If the operation is finished, we don't need to update the progress bar.
        if (remainingTicks > 0 || totalTicks > 0) {
            ChestMenuUtils.updateProgressbar(inv, slot, remainingTicks, totalTicks, getProgressBar());
        }
    }

    public AdvancedMachineProcessor(MachineProcessHolder<CraftingOperation> owner) {
        super(owner);
    }
}
