package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs;

import io.github.thebusybiscuit.slimefun4.implementation.items.electric.Capacitor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.RSCItemGroupLegacy;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;

public class CustomCapacitor extends Capacitor {
    @Override
    public void load() {
        if (!hidden) {
            RSCItemGroupLegacy.addItemToGroup(getItemGroup(), this);
        }

        getRecipeType().register(getRecipe(), getRecipeOutput());
    }

    public CustomCapacitor(YamlReader.BaseResult base, int capacity) {
        super(base.itemGroup(), capacity, base.sfis(), base.recipeType(), base.recipe());
    }
}
