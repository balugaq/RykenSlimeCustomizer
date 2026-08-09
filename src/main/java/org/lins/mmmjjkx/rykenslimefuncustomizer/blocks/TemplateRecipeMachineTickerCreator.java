package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;

import java.io.File;
import java.util.List;

@NullMarked
public class TemplateRecipeMachineTickerCreator extends RecipeMachineTickerCreator {
    @Override
    public @Nullable List<? extends AbstractRecipe> read(File file, int inputSize, int outputSize, ConfigurationSection section, ProjectAddon addon) {
        // todo
    }
}
