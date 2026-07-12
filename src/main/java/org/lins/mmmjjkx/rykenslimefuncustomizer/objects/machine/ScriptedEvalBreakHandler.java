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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import java.util.List;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;

@SuppressWarnings("deprecation")
public class ScriptedEvalBreakHandler extends BlockBreakHandler {
    private final ScriptEval eval;
    private final InventoryBlock machine;

    public ScriptedEvalBreakHandler(InventoryBlock machine, ScriptEval eval) {
        super(false, false);

        this.eval = eval;
        this.machine = machine;
    }

    @Override
    public void onPlayerBreak(
            BlockBreakEvent blockBreakEvent, @NotNull ItemStack itemStack, @NotNull List<ItemStack> list) {
        Block block = blockBreakEvent.getBlock();
        Location loc = block.getLocation();
        BlockMenu bm = StorageCacheUtils.getMenu(loc);
        if (bm != null) {
            if (machine.getInputSlots().length > 0) {
                bm.dropItems(loc, machine.getInputSlots());
            }
            if (machine.getOutputSlots().length > 0) {
                bm.dropItems(loc, machine.getOutputSlots());
            }
        }

        if (eval != null) {
            eval.evalFunction("onBreak", blockBreakEvent, itemStack, list);
        }
    }
}
