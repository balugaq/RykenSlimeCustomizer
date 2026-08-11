package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;

import java.io.File;
import java.util.List;

@NullMarked
@FunctionalInterface
public interface RecipeReader {
    static void addToList(
        List<CustomMachineRecipe> list,
        ConfigurationSection recipes,
        int seconds,
        List<InputWrapper> input,
        IntList chances,
        ItemStack[] output) {

        boolean chooseOne = recipes.getBoolean("chooseOne", false);
        boolean forDisplay = recipes.getBoolean("forDisplay", false);
        boolean hide = recipes.getBoolean("hide", false);

        output = CommonUtils.removeNulls(output);

        list.add(new CustomMachineRecipe(seconds, input, output, chances, chooseOne, forDisplay, hide));
    }

    @Nullable
    List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon);
}
