package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@NullMarked
@FunctionalInterface
public interface RecipeReader {
    static void addToList(
        List<CustomMachineRecipe> list,
        ConfigurationSection recipes,
        int seconds,
        ItemStack[] input,
        IntList chances,
        ItemStack[] output) {

        boolean chooseOne = recipes.getBoolean("chooseOne", false);
        boolean forDisplay = recipes.getBoolean("forDisplay", false);
        boolean hide = recipes.getBoolean("hide", false);

        ConfigurationSection inputSection = recipes.getConfigurationSection("input");
        IntList noConsumes = new IntArrayList();
        if (inputSection != null) {
            List<String> keys = new ArrayList<>(inputSection.getKeys(false));
            for (String s : keys) {
                ConfigurationSection section = inputSection.getConfigurationSection(s);
                if (section == null) {
                    continue;
                }

                if (section.getBoolean("noConsume", false)) {
                    noConsumes.add(keys.indexOf(s));
                }
            }

            boolean noConsume = recipes.getBoolean("noConsume", false);
            if (noConsume) {
                noConsumes.clear();
                noConsumes.addAll(IntStream.rangeClosed(0, keys.size()).boxed().toList());
            }
        }

        input = CommonUtils.removeNulls(input);
        output = CommonUtils.removeNulls(output);

        list.add(new CustomMachineRecipe(seconds, input, output, chances, chooseOne, forDisplay, hide, noConsumes));
    }

    @Nullable
    List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon);
}
