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

import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.script.ScriptEval;

@NullMarked
public class CustomMaterialGenerator extends AdvancedCustomMachine {
    private final int status;
    public CustomMaterialGenerator(YamlReader.BaseResult base, int[] input, int[] output, int energyPerCraft, int capacity, @Nullable ScriptEval eval, int status) {
        super(base, input, output, energyPerCraft, capacity, 1, eval);
        this.status = status;
    }

    @Override
    public void onNewInstance(BlockMenu menu, Block b) {
        menu.addMenuClickHandler(status, ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public boolean tick() {
        return false;
    }
}
