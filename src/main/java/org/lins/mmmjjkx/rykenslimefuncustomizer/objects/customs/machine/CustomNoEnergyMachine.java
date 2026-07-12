/*
 * RykenSlimefunCustomizer
 * Copyright (C) 2026 lijinhong11(mmmjjjkx) and balugaq
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.parent.AbstractEmptyMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.ScriptedEvalBreakHandler;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.SmallerMachineInfo;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.lambda.RSCClickHandler;

@SuppressWarnings("deprecation")
public class CustomNoEnergyMachine extends AbstractEmptyMachine<MachineOperation> {
    private final List<Integer> input;
    private final List<Integer> output;
    private final @Nullable ScriptEval eval;
    private final MachineProcessor<MachineOperation> processor;

    @Getter
    private final CustomMenu menu;

    public CustomNoEnergyMachine(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            CustomMenu menu,
            List<Integer> input,
            List<Integer> output,
            @Nullable ScriptEval eval,
            int work) {
        this(itemGroup, item, recipeType, recipe, menu, input, output, eval, Collections.singletonList(work));
    }

    public CustomNoEnergyMachine(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            CustomMenu menu,
            List<Integer> input,
            List<Integer> output,
            @Nullable ScriptEval eval,
            List<Integer> work) {
        super(itemGroup, item, recipeType, recipe);

        this.input = input;
        this.output = output;
        this.eval = eval;
        this.processor = new MachineProcessor<>(this);
        this.menu = menu;

        if (eval != null) {
            eval.addThing("setWorking", (Consumer<Boolean>) b -> eval.addThing("working", b));
            eval.addThing("working", false);

            eval.doInit();

            addItemHandler(
                    new BlockPlaceHandler(false) {
                        @Override
                        public void onPlayerPlace(@NotNull BlockPlaceEvent e) {
                            CustomNoEnergyMachine.this.eval.evalFunction("onPlace", e);
                        }
                    },
                    (BlockUseHandler) e -> CustomNoEnergyMachine.this.eval.evalFunction("onUse", e),
                    new BlockBreakHandler(false, false) {
                        @Override
                        public void onPlayerBreak(
                                @NotNull BlockBreakEvent e, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
                            MachineOperation operation = getMachineProcessor().getOperation(e.getBlock());
                            if (operation != null) {
                                getMachineProcessor().endOperation(e.getBlock());
                            }

                            CustomNoEnergyMachine.this.eval.evalFunction("onBreak", e, item, drops);
                        }
                    });
        }

        addItemHandler(new ScriptedEvalBreakHandler(this, eval));
        addItemHandler(getBlockTicker());

        if (this.menu != null) {
            for (int workSlot : work) {
                if (workSlot > -1 && workSlot < 54) {
                    ChestMenu.MenuClickHandler mcl = this.menu.getMenuClickHandler(workSlot);
                    this.menu.addMenuClickHandler(workSlot, new RSCClickHandler() {
                        @Override
                        public void mainFunction(Player player, int slot, ItemStack itemStack, ClickAction action) {
                            if (eval != null) {
                                eval.addThing("working", true);
                            }
                        }

                        @Override
                        public void andThen(Player player, int slot, ItemStack itemStack, ClickAction action) {
                            if (mcl != null) {
                                mcl.onClick(player, slot, itemStack, action);
                            }
                        }
                    });
                }
                this.processor.setProgressBar(menu.getProgressBarItem());
            }

            createPreset(this, this.menu::apply);
        }
    }

    @Override
    public void preRegister() {
        super.preRegister();
        this.addItemHandler(getBlockTicker());
    }

    protected void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
        if (eval != null) {
            SmallerMachineInfo info = new SmallerMachineInfo(data.getBlockMenu(), data, this, item, b, processor);
            eval.evalFunction("tick", info);
        }
    }

    @Override
    public int[] getInputSlots() {
        return input.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public int[] getOutputSlots() {
        return output.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public BlockTicker getBlockTicker() {
        return new BlockTicker() {
            @Override
            public boolean isSynchronized() {
                return true;
            }

            @Override
            public void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
                CustomNoEnergyMachine.this.tick(b, item, data);
            }
        };
    }

    @NotNull @Override
    public MachineProcessor<MachineOperation> getMachineProcessor() {
        return processor;
    }
}
