package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.io.File;

@NullMarked
public interface MachineTicker extends DataCache, RecipesHolder, CustomMenuHolder, EnergyNetComponent {
    Access<AbstractRecipe> lastRecipeAccessor = () -> AbstractRecipe.class;

    Type getType();

    @Override
    SlimefunItem getSlimefunItem();

    @Override
    default EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    boolean preTick(Location location);

    void tick(Location location);

    default void init() {
        var menu = getCustomMenu();
        if (menu == null) {
            Debug.warn("未找到菜单 " + getSlimefunItem().getId() + " 使用默认菜单");
            this.createPreset(getSlimefunItem(), getSlimefunItem().getItemName(), preset -> {
                CustomMenuHolder.constructMenu(preset, getProgressSlot(), getProgressBar());
            });
            return;
        }
        createPreset(getSlimefunItem(), menu::apply);
        if (menu.getProgressBar() != null) {
            getMachineProcessor().setProgressBar(menu.getProgressBar());
        }
    }

    int getEnergyConsumption();

    default boolean takeCharge(Location l) {
        if (isChargeable()) {
            int charge = getCharge(l);

            if (charge < getEnergyConsumption()) {
                return false;
            }

            setCharge(l, charge - getEnergyConsumption());
        }
        return true;
    }

    default boolean canTick(Location location) {
        return SuperMultiBlockManager.canTick(location);
    }

    @Nullable static MachineTicker create(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon, @Nullable Type type) {
        if (type == null) {
            var tpc = section.getString("type");
            var typeOptional = CommonUtils.getEnum(Type.class, tpc);
            if (typeOptional.isEmpty()) {
                Debug.warn(file, section, " 机器类型 (type) 非法: " + tpc + ", 已转换为 RECIPE");
                type = Type.RECIPE;
            } else {
                type = typeOptional.get();
            }
        }
        return type.createTicker(file, sf, section, menu, addon);
    }

    default void createGUI(Player p, int index) {
        if (index >= getRecipes().size()) return;
        AbstractRecipe recipe = getRecipes().get(index);
        Recipe.openGUI(p, getCustomMenu(), getInputSlots(), getOutputSlots(), recipe, getSlimefunItem());
    }

    @NullMarked
    @Getter
    enum Type {
        RECIPE(new RecipeMachineTickerCreator()), // 配方机器
        LINKED_RECIPE(new LinkedRecipeMachineTickerCreator()), // 强配方机器
        TEMPLATE_RECIPE(new TemplateRecipeMachineTickerCreator()), // 模板配方
        MATERIAL_GENERATOR(new MaterialGeneratorMachineTickerCreator()), // 材料生成器
        WORKBENCH(new WorkbenchMachineTickerCreator()); // 工作台

        private final TickerCreator tickerCreator;

        public @Nullable MachineTicker createTicker(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon) {
            return tickerCreator.create(file, sf, section, menu, addon);
        }


        Type(TickerCreator tickerCreator) {
            this.tickerCreator = tickerCreator;
        }
    }
}
