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

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.blocks.Recipe;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.AdvancedCustomMachine;

@SuppressWarnings("deprecation")
public class SingleItemRecipeGuideListener implements Listener {
    private static final int GUIDE_ITEM_OUTPUT_INDEX = 16;

    public SingleItemRecipeGuideListener() {
        Bukkit.getPluginManager().registerEvents(this, RykenSlimefunCustomizer.INSTANCE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        int index = pdc.getOrDefault(Recipe.RECIPE_INDEX_KEY, PersistentDataType.INTEGER, -1);
        if (index < 0) return;

        Inventory inv = e.getInventory();
        ItemStack sfItem = inv.getItem(GUIDE_ITEM_OUTPUT_INDEX);
        if (sfItem == null) return;

        SlimefunItem sfItemObj = SlimefunItem.getByItem(sfItem);
        if (sfItemObj == null) return;

        Player p = (Player) e.getWhoClicked();
        ChestMenu menu = createGUI(p, sfItemObj, item.getItemMeta());
        if (menu != null) {
            menu.open(p);
        }
    }
    private @Nullable ChestMenu createGUI(Player p, SlimefunItem machine, PersistentDataHolder holder) {
        int idx = PersistentDataAPI.getInt(holder, Recipe.RECIPE_INDEX_KEY, 0);
        if (machine instanceof AdvancedCustomMachine acm) {
            acm.openGUI(p, idx);
        }

        if (machine instanceof AContainer ac) {
            Recipe.openGUI(p, ac, idx);
        }

        return null;
    }
}
