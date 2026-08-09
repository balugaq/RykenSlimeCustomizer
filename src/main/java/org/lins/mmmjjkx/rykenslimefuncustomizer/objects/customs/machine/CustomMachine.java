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

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunUniversalData;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.attributes.UniversalBlock;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.parent.AbstractEmptyMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.MachineInfo;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.MachineRecord;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.ScriptedEvalBreakHandler;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class CustomMachine extends AbstractEmptyMachine<MachineOperation> implements EnergyNetComponent {
    private final MachineRecord theRecord;
    private final List<Integer> input;
    private final List<Integer> output;
    private final EnergyNetComponentType type;
    private final MachineProcessor<MachineOperation> processor;
    private final @Nullable ScriptEval eval;

    @Getter
    private final CustomMenu menu;

    public SlimefunItem getSlimefunItem() {
        return this;
    }

    public CustomMachine(
            YamlReader.BaseResult base,
            @Nullable CustomMenu menu,
            List<Integer> input,
            List<Integer> output,
            MachineRecord record,
            EnergyNetComponentType type,
            @Nullable ScriptEval eval) {
        super(base.itemGroup(), base.sfis(), base.recipeType(), base.recipe());

        this.input = input;
        this.output = output;
        this.theRecord = record;
        this.menu = menu;

        this.type = type;
        this.eval = eval;
        this.processor = new MachineProcessor<>(this);

        if (eval != null) {
            eval.doInit();

            addItemHandler(
                    new BlockPlaceHandler(false) {
                        @Override
                        public void onPlayerPlace(@NotNull BlockPlaceEvent e) {
                            CustomMachine.this.eval.evalFunction("onPlace", e);
                        }
                    },
                    (BlockUseHandler) e -> {
                        CustomMachine.this.eval.evalFunction("onUse", e);
                        if (!e.getInteractEvent().isCancelled()) {
                            if (BlockMenuPreset.isInventory(CustomMachine.this.getId())) {
                                openInventory(e.getPlayer(), getSlimefunItem(), e.getInteractEvent().getClickedBlock(), e);
                            }
                        }
                    },
                    new BlockBreakHandler(false, false) {
                        @Override
                        public void onPlayerBreak(
                                @NotNull BlockBreakEvent e, @NotNull ItemStack item, @NotNull List<ItemStack> drops) {
                            MachineOperation operation = getMachineProcessor().getOperation(e.getBlock());
                            if (operation != null) {
                                getMachineProcessor().endOperation(e.getBlock());
                            }

                            CustomMachine.this.eval.evalFunction("onBreak", e, item, drops);
                        }
                    });
        }

        addItemHandler(new ScriptedEvalBreakHandler(this, eval));

        if (menu != null) {
            this.processor.setProgressBar(menu.getProgressBarItem());

            createPreset(this, menu::apply);
        }
    }

    @Override
    public void preRegister() {
        super.preRegister();
        this.addItemHandler(getBlockTicker());
    }

    protected void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
        if (!SuperMultiBlockManager.canTick(b.getLocation())) return;
        if (eval != null) {
            BlockMenu blockMenu = StorageCacheUtils.getMenu(b.getLocation());
            MachineInfo info = new MachineInfo(blockMenu, data, item, b, processor, null, this);
            eval.evalFunction("tick", info);
        }
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
                CustomMachine.this.tick(b, item, data);
            }
        };
    }

    @Override
    public int[] getInputSlots() {
        return input.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public int[] getOutputSlots() {
        return output.stream().mapToInt(i -> i).toArray();
    }

    @NotNull @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return type;
    }

    @Override
    public int getCapacity() {
        return theRecord.capacity();
    }

    @NotNull @Override
    public MachineProcessor<MachineOperation> getMachineProcessor() {
        return processor;
    }

    @ParametersAreNonnullByDefault
    private void openInventory(Player p, SlimefunItem item, Block clickedBlock, PlayerRightClickEvent event) {
        try {
            if (!p.isSneaking() || event.getItem().getType() == Material.AIR) {
                event.getInteractEvent().setCancelled(true);

                if (item instanceof UniversalBlock) {
                    var uniData = StorageCacheUtils.getUniversalBlock(clickedBlock);

                    if (uniData == null) {
                        return;
                    }

                    if (uniData.isDataLoaded()) {
                        openMenu(uniData.getMenu(), clickedBlock, p);
                    } else {
                        Slimefun.getDatabaseManager()
                                .getBlockDataController()
                                .loadUniversalDataAsync(uniData, new IAsyncReadCallback<>() {
                                    @Override
                                    public boolean runOnMainThread() {
                                        return true;
                                    }

                                    @Override
                                    public void onResult(SlimefunUniversalData result) {
                                        if (!p.isOnline()) {
                                            return;
                                        }

                                        openMenu(result.getMenu(), clickedBlock, p);
                                    }
                                });
                    }
                } else {
                    var blockData = StorageCacheUtils.getBlock(clickedBlock.getLocation());

                    if (blockData == null) {
                        return;
                    }

                    if (blockData.isDataLoaded()) {
                        openMenu(blockData.getBlockMenu(), clickedBlock, p);
                    } else {
                        Slimefun.getDatabaseManager()
                                .getBlockDataController()
                                .loadBlockDataAsync(blockData, new IAsyncReadCallback<>() {
                                    @Override
                                    public boolean runOnMainThread() {
                                        return true;
                                    }

                                    @Override
                                    public void onResult(SlimefunBlockData result) {
                                        if (!p.isOnline()) {
                                            return;
                                        }

                                        openMenu(result.getBlockMenu(), clickedBlock, p);
                                    }
                                });
                    }
                }
            }
        } catch (Exception | LinkageError x) {
            item.error("An Exception was caught while trying to open the Inventory", x);
        }
    }

    private void openMenu(DirtyChestMenu menu, Block b, Player p) {
        if (menu != null) {
            if (menu.canOpen(b, p)) {
                menu.open(p);
            } else {
                Slimefun.getLocalization().sendMessage(p, "inventory.no-access", true);
            }
        }
    }
}
