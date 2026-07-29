package org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BlockDisplayDescriptor implements DisplayDescriptor {
    private final BlockData blockData;

    @Override
    public @NotNull Display createDisplay(Location location) {
        BlockDisplay display = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        display.setBlock(blockData);
        return display;
    }
}
