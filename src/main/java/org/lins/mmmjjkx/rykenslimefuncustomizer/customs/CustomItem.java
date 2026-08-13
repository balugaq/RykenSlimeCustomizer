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

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.RSCItemGroupLegacy;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;

public class CustomItem extends SlimefunItem {
    private final YamlReader.BaseResult base;

    public CustomItem(YamlReader.BaseResult base) {
        super(base.itemGroup(), base.sfis(), base.recipeType(), base.recipe(), base.output());
        this.base = base;
    }

    @Override
    public void load() {
        if (!hidden) {
            RSCItemGroupLegacy.addItemToGroup(getItemGroup(), this);
        }

        getRecipeType().register(getRecipe(), getRecipeOutput());
    }

    public Object[] constructorArgs() {
        return new Object[]{base};
    }
}
