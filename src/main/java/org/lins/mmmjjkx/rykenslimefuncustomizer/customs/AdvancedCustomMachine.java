package org.lins.mmmjjkx.rykenslimefuncustomizer.customs;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.AbstractRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.recipes.CustomMenuHolder;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers.MachineTicker;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers.TemplateRecipeMachineTicker;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers.InvIndex;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.BaseRSCItemGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.script.ScriptEval;

import java.util.List;
import java.util.function.Consumer;

@NullMarked
@Getter
public class AdvancedCustomMachine extends AContainer implements RecipeDisplayItem {
    @Override
    public void load() {
        if (!hidden) {
            BaseRSCItemGroup.addItemToGroup(getItemGroup(), this);
        }

        getRecipeType().register(getRecipe(), getRecipeOutput());
    }

    private @UnknownNullability MachineTicker ticker;
    private final @Nullable ScriptEval eval;
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
        int speed,
        @Nullable ScriptEval eval
    ) {
        super(base.itemGroup(), base.sfis(), base.recipeType(), base.recipe(), base.output());
        this.input = input;
        this.output = output;
        this.energyPerCraft = energyPerCraft;
        this.capacity = capacity;
        this.speed = speed;
        this.eval = eval;

        setCapacity(capacity);
        setEnergyConsumption(energyPerCraft);
        setProcessingSpeed(speed);
    }

    public void setTicker(MachineTicker ticker) {
        this.ticker = ticker;
        ticker.init();
        register(RykenSlimefunCustomizer.INSTANCE);
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

                getTicker().getAdvancedMachineProcessor().endOperation(b);
            }
        };
    }

    public MachineTicker.Type getType() {
        return ticker.getType();
    }

    @Override
    public void registerDefaultRecipes() {
        if (getTicker() == null) return;
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
        if (ticker == null) return CustomMenuHolder.DEFAULT_PROGRESS_BAR; // see AContainer<init>, we'll set progress bar again in ticker#init
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
    public void createPreset(SlimefunItem item, String title, Consumer<BlockMenuPreset> setup) {

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

    public void openGUI(Player p, int index) {
        ticker.createGUI(p, index);
    }

    public @Nullable Value evalFunction(String methodName, @Nullable Object... args) {
        if (eval == null) return null;
        return eval.evalFunction(methodName, args);
    }
}
