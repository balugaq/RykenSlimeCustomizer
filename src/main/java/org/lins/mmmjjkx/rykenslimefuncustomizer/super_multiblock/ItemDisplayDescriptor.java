package org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ItemDisplayDescriptor implements DisplayDescriptor {
    private final ItemStack itemStack;

    @Override
    public @NotNull Display createDisplay(Location location) {
        ItemDisplay display = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        display.setItemStack(itemStack);
        return display;
    }
}
