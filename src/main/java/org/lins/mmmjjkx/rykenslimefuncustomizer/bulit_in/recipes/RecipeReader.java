package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes;

import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.addon.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.InputWrapper;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;

import java.io.File;
import java.util.List;

@NullMarked
@FunctionalInterface
public interface RecipeReader {
    static void addToList(
        List<CustomMachineRecipe> list,
        ConfigurationSection recipe,
        int seconds,
        List<InputWrapper> input,
        IntList chances,
        ItemStack[] output) {

        boolean chooseOne = recipe.getBoolean("chooseOne", false);
        boolean forDisplay = recipe.getBoolean("forDisplay", false);
        boolean hide = recipe.getBoolean("hide", false);
        boolean noConsumeAll = recipe.getBoolean("noConsume", false);

        output = CommonUtils.removeNulls(output);

        list.add(new CustomMachineRecipe(seconds, input, output, chances, chooseOne, forDisplay, hide, noConsumeAll));
    }

    @Nullable
    List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon);
}
