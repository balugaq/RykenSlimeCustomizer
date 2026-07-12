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
package org.lins.mmmjjkx.rykenslimefuncustomizer.listeners;

import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.global.DropFromBlock;

public class BlockListener implements Listener {
    public BlockListener() {
        Bukkit.getPluginManager().registerEvents(this, RykenSlimefunCustomizer.INSTANCE);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        List<DropFromBlock.Drop> drops = DropFromBlock.getDrops(block.getType());
        List<DropFromBlock.Drop> matchedDrops =
                drops.stream().filter(drop -> matchChance(drop.dropChance())).toList();
        if (matchedDrops.isEmpty()) return;

        for (DropFromBlock.Drop drop : matchedDrops) {
            block.getWorld().dropItemNaturally(e.getBlock().getLocation(), drop.itemStack());
        }
    }

    private static boolean matchChance(int chance) {
        if (chance >= 100) return true;

        Random rand = new Random();
        return rand.nextInt(100) < chance;
    }
}
