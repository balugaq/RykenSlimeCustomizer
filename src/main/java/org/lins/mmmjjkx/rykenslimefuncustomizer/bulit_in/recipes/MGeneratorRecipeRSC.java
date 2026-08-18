package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import me.matl114.logitech.utils.UtilClass.RecipeClass.MGeneratorRecipe;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.AsyncChanceRecipeTask;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.InvIndex;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.BlockMenuUtil;

import java.util.ArrayList;
import java.util.List;

@NullMarked
@Getter
public class MGeneratorRecipeRSC extends MGeneratorRecipe implements Recipe {
    private final IntList chances;
    private final boolean chooseOne;
    public MGeneratorRecipeRSC(int ticks, ItemStack[] output, IntList chances, boolean chooseOne) {
        super(ticks, new ItemStack[0], output);
        this.chances = chances;
        this.chooseOne = chooseOne;
    }

    @Override
    public void formatGUI(ChestMenu inv, int[] inputSlots, int[] outputSlots) {
        // output - choose one
        if (isChooseOne()) {
            DoubleList weightedChance = new DoubleArrayList();
            int allChance = getChances().intStream().sum();
            for (int i = 0; i < getOutput().length; i++) {
                int chance = getChances().getInt(i);
                weightedChance.add((double) chance / allChance);
            }

            List<ItemStack> cycles = new ArrayList<>();
            for (int i = 0; i < getOutput().length; i++) {
                cycles.add(Recipe.tagOutputChance(getOutput()[i], weightedChance.getDouble(i))); // 保留 1 位小数
            }

            AsyncChanceRecipeTask task = new AsyncChanceRecipeTask();
            task.add(outputSlots[0], cycles);
            task.start(inv.getInventory());
            return;
        }

        // output - normal
        boolean overflowed = false;
        for (int i = 0; i < getOutput().length; i++) {
            if (i == outputSlots.length) {
                overflowed = true;
            }
            if (overflowed) break;

            ItemStack output = getOutput()[i];
            int chance = getChances().getInt(i);
            inv.addItem(outputSlots[i], Recipe.tagOutputChance(output, chance), ChestMenuUtils.getEmptyClickHandler());
        }
        if (overflowed) {
            // generate info stack at the last slot
            inv.addItem(outputSlots[outputSlots.length - 1], Recipe.asOutputInfoStack(getOutput(), getChances()), ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public boolean matches(InvIndex index, boolean consumeItems) {
        return true;
    }

    @Override
    public boolean pushOutputs(BlockMenu inv) {
        ItemStack[] clone = new ItemStack[getOutput().length];
        for (int i = 0; i < getOutput().length; i++) {
            clone[i] = getOutput()[i].clone();
        }
        return BlockMenuUtil.pushItem(inv, clone, inv.getPreset().getSlotsAccessedByItemTransport(ItemTransportFlow.WITHDRAW)).isEmpty();
    }

    @Override
    public <T extends MachineRecipe & Recipe> T asMachineRecipe() {
        return (T) this;
    }

    @Override // never called
    public ItemStack getDisplayInput(int index) {
        return null;
    }

    @Override // never called
    public ItemStack getDisplayOutput(int index) {
        return null;
    }
}
