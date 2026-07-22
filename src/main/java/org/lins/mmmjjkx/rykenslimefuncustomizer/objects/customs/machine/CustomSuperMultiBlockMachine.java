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

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.services.sounds.SoundEffect;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.m;
import org.jetbrains.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.libraries.colors.CMIChatColor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.CustomMachineRecipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlock;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockDefinition;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockManager;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;

@Getter
public class CustomSuperMultiBlockMachine extends CustomRecipeMachine {
    private final ScriptEval eval;
    private final SuperMultiBlockDefinition definition;
    private final boolean displayProjectiles;

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
            @Nullable SuperMultiBlockDefinition definition,
            boolean displayProjectiles) {
        super(itemGroup, item, recipeType, recipe, input, output, recipes, energyPerCraft, capacity, menu, speed, hideAllRecipes);

        this.eval = eval;
        this.definition = definition;
        this.displayProjectiles = displayProjectiles;

        addItemHandler(new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(BlockBreakEvent event, ItemStack item, List<ItemStack> drops) {
                firstTicks.remove(event.getBlock().getLocation());
            }
        });
        register(RykenSlimefunCustomizer.INSTANCE);
    }

    private final Set<Location> firstTicks = new HashSet<>();

    @Override
    protected void tick(Block b) {
        super.tick(b);
        if (firstTicks.add(b.getLocation())) {
            if (!SuperMultiBlockManager.getInstance().startSuperMultiBlock(new SuperMultiBlock(CustomSuperMultiBlockMachine.this, b.getLocation()))) {
                b.getWorld().getNearbyPlayers(b.getLocation(), 10, 10, 10).forEach(p -> {
                    p.sendMessage(CMIChatColor.colorize("&c附近存在其他多方块阻碍，无法搭建该多方块，请拆除后重试。"));
                    if (eval != null) {
                        eval.evalFunction("cannotStartSuperMultiBlock", b, this);
                    }
                });
            }
        }
        if (eval != null) {
            eval.evalFunction("onTick", b, this);
        }
    }

    public void onFormed() {
        if (eval != null) {
            eval.evalFunction("onFormed", this);
        }
    }

    public void onUnformed() {
        if (eval != null) {
            eval.evalFunction("onUnformed", this);
        }
    }

    public void onInteract(PlayerInteractEvent event, SuperMultiBlock instance) {
        if (eval != null) {
            eval.evalFunction("onInteract", event, this);
        } else {
            var menu = StorageCacheUtils.getMenu(instance.getCoreLocation());
            if (menu != null) {
                menu.open(event.getPlayer());
            }
        }
    }
}