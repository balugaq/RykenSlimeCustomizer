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

import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.attributes.RandomMobDrop;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import lombok.Getter;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.groups.BaseRSCItemGroup;
import org.lins.mmmjjkx.rykenslimefuncustomizer.readers.YamlReader;

import java.util.HashSet;
import java.util.Set;

public class CustomMobDrop extends CustomItem implements RandomMobDrop, NotPlaceable {
    private final int chance;

    @Getter
    private final EntityType entityType;

    public CustomMobDrop(YamlReader.BaseResult base, int chance, EntityType type) {
        super(base);
        this.chance = chance;
        this.entityType = type;

        register(RykenSlimefunCustomizer.INSTANCE);
    }

    @Override
    public int getMobDropChance() {
        return chance >= 100 ? 100 : Math.max(chance, 1);
    }

    @Override
    public void load() {
        if (!hidden) {
            BaseRSCItemGroup.addItemToGroup(getItemGroup(), this);
        }

        Set<ItemStack> dropping = Slimefun.getRegistry().getMobDrops().getOrDefault(entityType, new HashSet<>());
        dropping.add(this.getItem());
        Slimefun.getRegistry().getMobDrops().put(entityType, dropping);
    }
}
