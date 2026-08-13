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
package org.lins.mmmjjkx.rykenslimefuncustomizer.customs;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Value;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.menu.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.script.ScriptEval;

@NullMarked
public class CustomWorkbench extends AdvancedCustomMachine {
    private final int click;
    public CustomWorkbench(YamlReader.BaseResult base, int[] input, int[] output, int energyPerCraft, int capacity, int click, @Nullable CustomMenu menu, @Nullable ScriptEval eval) {
        super(base, input, output, energyPerCraft, capacity, 1, eval);
        this.click = click;
        new BlockMenuPreset(this.getId(), this.getItemName()) {
            public void init() {
                if (menu != null) {
                    menu.apply(this);
                }
            }

            public void newInstance(BlockMenu menu, Block b) {
                menu.addMenuClickHandler(
                    CustomWorkbench.this.click, (player, clickedSlot, clickedItem, clickAction) -> {
                        var result = evalFunction("onClick", this, player, clickedSlot, clickedItem, clickAction);
                        if (result != null) {
                            return result.asBoolean();
                        }

                        if (!takeCharge(menu.getLocation())) {
                            return false;
                        }

                        var recipe = findNextRecipe(menu);
                        if (recipe != null) {
                            recipe.pushOutputs(menu);
                        }
                        return false;
                    });
            }

            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return flow == ItemTransportFlow.INSERT ? input : output;
            }

            public boolean canOpen(Block b, Player p) {
                return p.isOp()
                    || p.hasPermission("slimefun.inventory.bypass")
                    || (CustomWorkbench.this.canUse(p, false)
                        && Slimefun.getProtectionManager().hasPermission(p, b.getLocation(), Interaction.INTERACT_BLOCK));
            }
        };
    }

    @Override
    public boolean tick() {
        return false;
    }
}
