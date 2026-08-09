package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;

@NullMarked
public interface CustomMenuHolder extends InventoryBlock {
    ItemStack DEFAULT_PROGRESS_BAR = new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE);
    int[] DEFAULT_BORDER = {0, 1, 2, 3, 4, 5, 6, 7, 8, 13, 31, 36, 37, 38, 39, 40, 41, 42, 43, 44};
    int[] DEFAULT_BORDER_IN = {9, 10, 11, 12, 18, 21, 27, 28, 29, 30};
    int[] DEFAULT_BORDER_OUT = {14, 15, 16, 17, 23, 26, 32, 33, 34, 35};

    @Nullable CustomMenu getCustomMenu();

    SlimefunItem getSlimefunItem();
    
    default void constructMenu(BlockMenuPreset preset) {
        for (int i : DEFAULT_BORDER) {
            preset.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : DEFAULT_BORDER_IN) {
            preset.addItem(i, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : DEFAULT_BORDER_OUT) {
            preset.addItem(i, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(getProgressSlot(), getProgressBar(), ChestMenuUtils.getEmptyClickHandler());
    }

    default ItemStack getProgressBar() {
        if (getCustomMenu() == null || getCustomMenu().getProgressBarItem() == null) {
            return DEFAULT_PROGRESS_BAR;
        }
        return getCustomMenu().getProgressBarItem();
    }

    default int getProgressSlot() {
        if (getCustomMenu() == null || getCustomMenu().getProgressSlot() == -1) {
            return 22;
        }
        return getCustomMenu().getProgressSlot();
    }
}
