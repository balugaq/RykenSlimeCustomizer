package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.Capacitor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.RSCItemGroupLegacy;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;

import javax.annotation.Nonnull;

public class CustomCapacitor extends SlimefunItem implements EnergyNetComponent {
    private final int capacity;

    @Override
    public void load() {
        if (!hidden) {
            RSCItemGroupLegacy.addItemToGroup(getItemGroup(), this);
        }

        getRecipeType().register(getRecipe(), getRecipeOutput());
    }

    public CustomCapacitor(YamlReader.BaseResult base, int capacity) {
        super(base.itemGroup(), base.sfis(), base.recipeType(), base.recipe(), base.output());
        this.capacity = capacity;
    }

    @Override
    @Nonnull
    public final EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CAPACITOR;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }
}
