package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.AbstractRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.InvIndex;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.MachineTicker;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.TemplateRecipeMachineTicker;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.slimefun.RSCItemGroupLegacy;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;

import java.util.List;

@NullMarked
@Getter
public class AdvancedCustomMachine extends AContainer {
    @Override
    public void load() {
        if (!hidden) {
            RSCItemGroupLegacy.addItemToGroup(getItemGroup(), this);
        }

        getRecipeType().register(getRecipe(), getRecipeOutput());
    }

    private MachineTicker ticker;
    private final int[] input;
    private final int[] output;
    private final int energyPerCraft;
    private final int capacity;
    private final int speed;
    public AdvancedCustomMachine(
        YamlReader.BaseResult base,
        int[] input,
        int[] output,
        int energyPerCraft,
        int capacity,
        int speed
    ) {
        super(base.itemGroup(), base.sfis(), base.recipeType(), base.recipe(), base.output());
        this.input = input;
        this.output = output;
        this.energyPerCraft = energyPerCraft;
        this.capacity = capacity;
        this.speed = speed;

        setCapacity(capacity);
        setEnergyConsumption(energyPerCraft);
        setProcessingSpeed(speed);

        if (register()) {
            register(RykenSlimefunCustomizer.INSTANCE);
        }
    }

    public void setTicker(MachineTicker ticker) {
        this.ticker = ticker;
        ticker.init();
    }

    protected boolean register() {
        return true;
    }

    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            public void onBlockBreak(Block b) {
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());
                if (inv != null) {
                    if (ticker instanceof TemplateRecipeMachineTicker tp) {
                        inv.dropItems(b.getLocation(), tp.getTemplateSlot());
                    }
                    inv.dropItems(b.getLocation(), getInputSlots());
                    inv.dropItems(b.getLocation(), getOutputSlots());
                }

                getMachineProcessor().endOperation(b);
            }
        };
    }

    public MachineTicker.Type getType() {
        return ticker.getType();
    }

    @Override
    protected void registerDefaultRecipes() {
        getTicker().getRecipes().forEach(super::registerRecipe);
    }

    @Override
    public int getEnergyConsumption() {
        return energyPerCraft;
    }

    @Override
    public String getMachineIdentifier() {
        return ticker.getMachineIdentifier();
    }

    @Override
    public List<ItemStack> getDisplayRecipes() {
        return ticker.getDisplayRecipes();
    }

    @Override
    public ItemStack getProgressBar() {
        return ticker.getProgressBar();
    }

    @Override
    public int[] getInputSlots() {
        return input;
    }

    @Override
    public int[] getOutputSlots() {
        return output;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    protected void tick(Block b) {
        if (tick()) {
            ticker.tick(b.getLocation());
        }
    }

    @Override
    @Nullable
    public AbstractRecipe findNextRecipe(BlockMenu inv) {
        InvIndex index = InvIndex.create(inv);
        var recipe = ticker.getCache(inv.getLocation(), MachineTicker.lastRecipeAccessor);
        if (recipe == null || !recipe.matches(index)) {
            recipe = ticker.findNextRecipe(index, recipe);
        }
        return recipe;
    }

    public boolean tick() {
        return true;
    }
}
