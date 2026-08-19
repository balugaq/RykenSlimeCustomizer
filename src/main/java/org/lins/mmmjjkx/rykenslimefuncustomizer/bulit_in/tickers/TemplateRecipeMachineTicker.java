package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CraftingRecipeOperation;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CustomTemplateMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.InvIndex;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.StackUtils;

@NullMarked
public interface TemplateRecipeMachineTicker extends MachineTicker {
    @Override
    default MachineTicker.Type getType() {
        return Type.TEMPLATE_RECIPE;
    }

    int getTemplateSlot();

    boolean isFasterIfMoreTemplates();

    @Override
    default boolean preTick(Location location) {
        return takeCharge(location);
    }

    default void tick(Location location) {
        if (!canTick(location) || !preTick(location)) return;

        BlockMenu inv = StorageCacheUtils.getMenu(location);
        if (inv == null) return;

        var currentOperation = getCurrentOperation(location);
        if (currentOperation == null) {
            InvIndex index = InvIndex.create(inv);
            var recipe = getCache(location, lastRecipeAccessor);
            if (recipe == null || !recipe.matches(index)) {
                recipe = findNextRecipe(index, recipe);
                if (recipe == null) return;
                setCache(location, lastRecipeAccessor, recipe);
            }

            currentOperation = new CraftingRecipeOperation(recipe);
            getAdvancedMachineProcessor().startOperation(location, currentOperation);
            if (currentOperation.isFinished()) {
                finishOperation(currentOperation, inv, location);
            } else {
                if (inv.hasViewer()) {
                    getAdvancedMachineProcessor().updateProgressBar(inv, getProgressSlot(), currentOperation);
                }
            }
            return;
        }

        if (!currentOperation.isFinished()) {
            ItemStack template = inv.getItemInSlot(getTemplateSlot());
            if (template == null
                || template.getType().isAir()
                || !StackUtils.itemsMatch(template, ((CustomTemplateMachineRecipe) currentOperation.getRecipe()).getTemplateStack())) {
                getAdvancedMachineProcessor().endOperation(location); // cancel operation
                return;
            }
            // no negative amount
            currentOperation.addProgress(isFasterIfMoreTemplates() ? Math.min(currentOperation.getRemainingTicks(), template.getAmount()) : 1);
            getAdvancedMachineProcessor().updateProgressBar(inv, getProgressSlot(), currentOperation);
            return;
        }

        finishOperation(currentOperation, inv, location);
    }

    default void finishOperation(CraftingRecipeOperation currentOperation, BlockMenu inv, Location location) {
        // finish recipe
        currentOperation.getRecipe().pushOutputs(inv);

        if (inv.hasViewer()) {
            ItemStack progress;
            if (getCustomMenu() == null) {
                progress = ChestMenuUtils.getBackground();
            } else {
                progress = getCustomMenu().getItems().getOrDefault(getProgressSlot(), ChestMenuUtils.getBackground());
            }
            inv.replaceExistingItem(getProgressSlot(), progress);
        }

        getAdvancedMachineProcessor().endOperation(location);
    }
}
