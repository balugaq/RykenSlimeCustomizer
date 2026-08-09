package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;

@NullMarked
public interface MachineTicker extends DataCache, RecipesHolder, CustomMenuHolder, EnergyNetComponent {
    Access<AbstractRecipe> lastRecipeAccessor = () -> AbstractRecipe.class;

    Type getType();

    @Override
    default EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    boolean preTick(Location location);

    void tick(Location location);

    // todo: 别忘了call这个
    default void init() {
        var menu = getCustomMenu();
        if (menu == null) {
            ExceptionHandler.handleWarning("未找到菜单 " + getSlimefunItem().getId() + " 使用默认菜单");
            this.createPreset(getSlimefunItem(), getSlimefunItem().getItemName(), this::constructMenu);
            return;
        }
        createPreset(getSlimefunItem(), menu::apply);
        if (menu.getProgressBarItem() != null) {
            getMachineProcessor().setProgressBar(menu.getProgressBarItem());
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
        // todo
        // type: RECIPE
        // recipes:
        // hideAllRecipes: boolean
        // 主要是读配方部分
        if (type == null) type = Type.RECIPE;
        return type.createTicker(file, sf, section, menu, addon);
    }

    @NullMarked
    @Getter
    enum Type {
        RECIPE(new RecipeMachineTickerCreator()), // 配方机器
        LINKED_RECIPE(new LinkedRecipeMachineTickerCreator()), // 强配方机器
        TEMPLATE_RECIPE(new TemplateRecipeMachineTickerCreator()), // 模板配方
        MATERIAL_GENERATOR(new MaterialGeneratorMachineTickerCreator()), // 材料生成器
        WORKBENCH(LINKED_RECIPE); // 工作台，记得去掉ticker

        private final TickerCreator tickerCreator;

        public @Nullable MachineTicker createTicker(File file, AdvancedCustomMachine sf, ConfigurationSection section, @Nullable CustomMenu menu, ProjectAddon addon) {
            return tickerCreator.create(file, sf, section, menu, addon);
        }


        Type(TickerCreator tickerCreator) {
            this.tickerCreator = tickerCreator;
        }

        Type(Type type) {
            this(type.tickerCreator);
        }
    }
}
