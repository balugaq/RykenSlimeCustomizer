package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CraftingRecipeOperation;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.InvIndex;

@NullMarked
public interface RecipeMachineTicker extends MachineTicker {
    @Override
    default Type getType() {
        return Type.RECIPE;
    }

    @Override
    default boolean preTick(Location location) {
        return takeCharge(location);
    }

    @Override
    default void tick(Location location) {
        if (!canTick(location) || !preTick(location)) return;

        BlockMenu inv = StorageCacheUtils.getMenu(location);
        if (inv == null) return;

        CraftingRecipeOperation currentOperation = getCurrentOperation(location);
        if (currentOperation == null) {
            InvIndex index = InvIndex.create(inv);
            var recipe = getCache(location, lastRecipeAccessor);
            if (recipe == null || !recipe.matches(index)) {
                recipe = findNextRecipe(index, recipe);
                if (recipe == null) return;
            }

            currentOperation = new CraftingRecipeOperation(recipe);
            getAdvancedMachineProcessor().startOperation(location, currentOperation);
            getAdvancedMachineProcessor().updateProgressBar(inv, getProgressSlot(), currentOperation);
            return;
        }

        if (!currentOperation.isFinished()) {
            getAdvancedMachineProcessor().updateProgressBar(inv, getProgressSlot(), currentOperation);
            currentOperation.addProgress(1);
            return;
        }

        // finish recipe
        currentOperation.getRecipe().pushOutputs(inv);

        ItemStack progress;
        if (getCustomMenu() == null) {
            progress = ChestMenuUtils.getBackground();
        } else {
            progress = getCustomMenu().getItems().getOrDefault(getProgressSlot(), ChestMenuUtils.getBackground());
        }
        inv.replaceExistingItem(getProgressSlot(), progress);

        getAdvancedMachineProcessor().endOperation(location);
    }
}
