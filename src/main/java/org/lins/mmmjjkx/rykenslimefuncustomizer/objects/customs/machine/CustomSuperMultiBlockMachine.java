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
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import lombok.Data;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlock;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockDefinition;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockManager;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ReflectionUtils;

/**
 * JS:
 * onTick(block, machine, ctx)
 * onFormed(partLocation, machine)
 * onUnformed(partLocation, machine)
 * onDestroy(machine)
 * onInteract(event, machine)
 * isOfPart(location, multiblock)
 * cannotStartSuperMultiBlock(location, machine)
 * onClickedPartBlock(event, machine)
 * onClickedPartBlockNotFormed(event, machine)
 * autoSwitchedDisplayLayer(layer, machine)
 * switchDisplayLayer(layerIndex, machine)
 * onClickedPartBlockNotFormed(event, machine)
 * formedLayer(layer, machine)
 * -
 * ctx = TickContext
 * event = PlayerInteractEvent
 * machine = CustomSuperMultiBlockMachine
 * multiblock = SuperMultiBlock
 */
@Slf4j
@Getter
public class CustomSuperMultiBlockMachine extends CustomRecipeMachine {
    public static final int DISPLAY_ALL = -999;
    public static final ItemStack NOT_BUILT_YET = new CustomItemStack(Material.BRICKS, "&c多方块尚未搭建完成!", "");
    private final ScriptEval eval;
    private final SuperMultiBlockDefinition definition;
    private final boolean displayProjectiles;
    private final boolean checkFormed;
    private final boolean openMenuWhenClickedParts;
    private final boolean noMenuWhenNotFormed;
    private final boolean allowSwitchDisplayLayer;
    private final boolean defaultNotice;

    private static @Nullable Field MENU_FIELD = null;

    static {
        try {
            MENU_FIELD = SlimefunBlockData.class.getDeclaredField("menu");
            MENU_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            ExceptionHandler.handleError("Failed to get menu field from SlimefunBlockData.class.", e);
        }
    }

    public CustomSuperMultiBlockMachine(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int[] input,
            int[] output,
            List<CustomMachineRecipe> recipes,
            int energyPerCraft,
            int capacity,
            @Nullable CustomMenu menu,
            int speed,
            boolean hideAllRecipes,
            @Nullable ScriptEval eval,
            SuperMultiBlockDefinition definition,
            boolean displayProjectiles,
            boolean checkFormed,
            boolean openMenuWhenClickedParts,
            boolean noMenuWhenNotFormed,
            boolean allowSwitchDisplayLayer,
            boolean defaultNotice) {
        super(itemGroup, item, recipeType, recipe, input, output, recipes, energyPerCraft, capacity, menu, speed, hideAllRecipes);

        this.eval = eval;
        this.definition = definition;
        this.displayProjectiles = displayProjectiles;
        this.checkFormed = checkFormed;
        this.openMenuWhenClickedParts = openMenuWhenClickedParts;
        this.noMenuWhenNotFormed = noMenuWhenNotFormed;
        this.allowSwitchDisplayLayer = allowSwitchDisplayLayer;
        this.defaultNotice = defaultNotice;

        addItemHandler(new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@NonNull BlockPlaceEvent e) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(RykenSlimefunCustomizer.INSTANCE, () -> {
                    var data = StorageCacheUtils.getBlock(e.getBlock().getLocation());

                    try {
                        MENU_FIELD.set(data, new BlockMenu(data.getBlockMenu().getPreset(), e.getBlock().getLocation(), data.getBlockMenu().getContents()) {
                            @Override
                            public void open(Player... players) {
                                if (!noMenuWhenNotFormed || definition.isFullyFormedCached(e.getBlock().getLocation())) {
                                    super.open(players);
                                }
                            }
                        });
                    } catch (IllegalAccessException e2) {
                        ExceptionHandler.handleError("Failed to set menu field.", e2);
                    }
                }, 1L);
            }
        });

        register(RykenSlimefunCustomizer.INSTANCE);
    }

    @NotNull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return new SimpleBlockBreakHandler() {
            public void onBlockBreak(@NotNull Block b) {
                SuperMultiBlockManager.getInstance().destroySuperMultiBlock(b.getLocation());
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());
                if (inv != null) {
                    inv.dropItems(b.getLocation(), CustomSuperMultiBlockMachine.this.getInputSlots());
                    inv.dropItems(b.getLocation(), CustomSuperMultiBlockMachine.this.getOutputSlots());
                }

                CustomSuperMultiBlockMachine.this.getMachineProcessor().endOperation(b);
            }
        };
    }

    public static final Set<Location> firstTicks = new HashSet<>();

    @Data
    public static class TickContext {
        private boolean callSuper = true;
        private boolean checkFirstTick = true;
    }

    @Override
    protected void tick(Block b) {
        var ctx = new TickContext();
        if (eval != null) {
            eval.evalFunction("onTick", b, this, ctx);
        }
        if (ctx.checkFirstTick && firstTicks.add(b.getLocation())) {
            if (!SuperMultiBlockManager.getInstance().startSuperMultiBlock(new SuperMultiBlock(CustomSuperMultiBlockMachine.this, b.getLocation()))) {
                if (defaultNotice) {
                    SuperMultiBlockManager.findNearbyPlayers(b.getLocation(), 10, p -> {
                        p.sendMessage(CMIChatColor.colorize("&c附近存在其他多方块阻碍，无法搭建该多方块，请拆除后重试。"));
                    });
                }
                if (eval != null) {
                    eval.evalFunction("cannotStartSuperMultiBlock", b, this);
                }
            } else {
                if (defaultNotice) {
                    String click = noMenuWhenNotFormed ? "右键" : "左键";
                    SuperMultiBlockManager.findNearbyPlayers(b.getLocation(), 10, p -> {
                        p.sendMessage(CMIChatColor.colorize("&a你已放置 " + getItemName() + ". &a" + click + " 或 Shift+" + click + "以切换投影层."));
                    });
                }
            }
        }

        if (ctx.callSuper) {
            SuperMultiBlockManager.getInstance().markDirty(b.getLocation(), false); // to allow multiblock recursive building check
            if (checkFormed) {
                SuperMultiBlock smb = SuperMultiBlockManager.getInstance().getSuperMultiBlock(b.getLocation());
                if (smb == null || !smb.isFullyFormedCached()) {
                    return;
                }
            }
            super.tick(b);
        }
    }

    @Override
    protected boolean preTick(Block b, BlockMenu inv, int progressSlot) {
        if (!checkFormed) return true;
        SuperMultiBlock smb = SuperMultiBlockManager.getInstance().getSuperMultiBlock(b.getLocation());
        if (smb == null || !smb.isFullyFormedCached()) {
            inv.replaceExistingItem(progressSlot, NOT_BUILT_YET);
            return false;
        }
        return true;
    }

    public void onFormed(Location partLocation) {
        if (eval != null) {
            eval.evalFunction("onFormed", partLocation, this);
        } else {
            SuperMultiBlockManager.findNearbyPlayers(partLocation, 10, p -> {
                p.sendMessage(CMIChatColor.colorize("&a已搭建完成 " + getItemName()));
            });
        }
    }

    public void onUnformed(Location partLocation) {
        if (eval != null) {
            eval.evalFunction("onUnformed", partLocation, this);
        } else {
            SuperMultiBlockManager.findNearbyPlayers(partLocation, 10, p -> {
                p.sendMessage(CMIChatColor.colorize("&c" + getItemName() + "&c已被破坏!"));
            });
        }
    }

    public void formedLayer(int layer) {
        if (eval != null) {
            Bukkit.getScheduler().runTask(RykenSlimefunCustomizer.INSTANCE, () -> {
                eval.evalFunction("formedLayer", layer, this);
            });
        }
    }

    public void onDestroy() {
        if (eval != null) {
            eval.evalFunction("onDestroy", this);
        }
    }

    public void autoSwitchedNewLayer(int layer) {
        if (eval != null) {
            eval.evalFunction("autoSwitchedDisplayLayer", layer, this);
        }
    }

    public int getCurrentLayerIndex(SuperMultiBlock instance) {
        String layerS = StorageCacheUtils.getData(instance.getCoreLocation(), "layer");
        if (layerS == null) {
            return 0;
        }
        int layer;
        try {
            layer = Integer.parseInt(layerS);
        } catch (NumberFormatException ignored) {
            layer = 0;
        }
        return layer;
    }

    public void switchLayer(SuperMultiBlock instance, Player p, boolean down) {
        int layerIndex = getCurrentLayerIndex(instance);
        if (eval == null || eval.evalFunction("switchDisplayLayer", instance, layerIndex) == null && instance.getLayers().length > 1) {
            // call origin
            int oldLayer = instance.getLayers()[layerIndex];
            layerIndex += down ? -1 : 1;
            if (layerIndex == -1) layerIndex = instance.layerCount() - 1;
            if (layerIndex == instance.layerCount()) {
                // display all
                StorageCacheUtils.setData(instance.getCoreLocation(), "layer", "" + DISPLAY_ALL);
                SuperMultiBlockManager.getInstance().showEntities(SuperMultiBlockManager.getInstance().selectEntities(instance));
                p.sendMessage(CMIChatColor.colorize("&a已切换多方块显示层为所有层"));
                return;
            }
            if (layerIndex == -999) {
                SuperMultiBlockManager.getInstance().hideEntities(SuperMultiBlockManager.getInstance().selectEntities(instance));
                layerIndex = 0;
            }
            int newLayerIndex = layerIndex;
            int newLayer = instance.getLayers()[newLayerIndex];
            StorageCacheUtils.setData(instance.getCoreLocation(), "layer", "" + newLayerIndex);
            SuperMultiBlockManager.getInstance().updateLayer(instance, oldLayer, newLayer);
            p.sendMessage(CMIChatColor.colorize("&a已切换多方块显示层为 y=" + newLayer + " (第" + (newLayerIndex + 1) + "/" + (instance.layerCount()) + "层)"));
        }
    }

    public void onInteract(PlayerInteractEvent event, SuperMultiBlock instance) {
        if (eval != null && eval.evalFunction("onInteract", event, this) != null) {
            return;
        }

        boolean clickedCore = event.getClickedBlock().getLocation().equals(instance.getCoreLocation());
        if (allowSwitchDisplayLayer && !instance.isFullyFormedCached() && clickedCore) {
            boolean left = event.getAction().isLeftClick();
            boolean right = event.getAction().isRightClick();
            Player p = event.getPlayer();
            boolean shift = p.isSneaking();
            var tp = p.getInventory().getItemInMainHand().getType();
            boolean holdingBlock = tp.isBlock() && !tp.isAir();
            if (!holdingBlock && (!noMenuWhenNotFormed && left) || (noMenuWhenNotFormed && right)) {
                // switch display layer
                // shift = down, !shift = up
                switchLayer(instance, p, shift);
                return;
            }
        }

        if (!openMenuWhenClickedParts && !clickedCore) {
            if (eval != null) eval.evalFunction("onClickedPartBlock", event, this);
            return;
        }
        if (noMenuWhenNotFormed && !instance.isFullyFormedCached()) {
            if (eval != null) eval.evalFunction("onClickedPartBlockNotFormed", event, this);
            return;
        }
        var menu = StorageCacheUtils.getMenu(instance.getCoreLocation());
        if (menu != null) {
            menu.open(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @Override
    public boolean register() {
        return false;
    }
}