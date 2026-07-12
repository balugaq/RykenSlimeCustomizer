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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.enhanced;

import de.tr7zw.nbtapi.*;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class NBTAPIIntegration {
    public static NBTAPIIntegration instance = new NBTAPIIntegration();

    private NBTAPIIntegration() {}

    public ReadWriteNBT readItem(ItemStack item) {
        return NBT.itemStackToNBT(item);
    }

    public NBTCompound getOrCreateCompound(NBTCompound parent, String name) {
        return parent.getOrCreateCompound(name);
    }

    public NBTCompound readBlock(Block block) {
        return new NBTBlock(block).getData();
    }

    public ReadWriteNBT readEntity(Entity entity) {
        ReadWriteNBT entityNbt = NBT.createNBTObject();
        NBT.get(entity, entityNbt::mergeCompound);
        return entityNbt;
    }

    public ReadWriteNBT createCompound() {
        return NBT.createNBTObject();
    }
}
