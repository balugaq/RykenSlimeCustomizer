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
package org.lins.mmmjjkx.rykenslimefuncustomizer.customs.script_machine;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;

public record TickContextScriptMachineNoEnergy(
        @Nullable BlockMenu blockMenu,
        SlimefunBlockData data,
        ScriptMachineNoEnergy machine,
        SlimefunItem machineItem,
        Block block,
        MachineProcessor<?> processor) {

    public Inventory getInventory() {
        if (blockMenu == null) {
            return null;
        }
        return blockMenu.getInventory();
    }
}
