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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.lambda;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
@FunctionalInterface
public interface RSCClickHandler extends ChestMenu.MenuClickHandler {
    void mainFunction(Player player, int slot, ItemStack itemStack, ClickAction action);

    default boolean onClick(Player var1, int var2, ItemStack var3, ClickAction var4) {
        mainFunction(var1, var2, var3, var4);
        andThen(var1, var2, var3, var4);
        return false;
    }

    default void andThen(Player player, int slot, ItemStack itemStack, ClickAction action) {}
}
