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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.generations;

import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.World;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.Range;

@Getter
@Setter
public class GenerationArea {
    private Range height;
    private int most;
    private int amount;
    private Range size;
    private Material replacement;
    private World.Environment environment;

    public GenerationArea(
            @Nonnull Range height,
            int most,
            int amount,
            @Nonnull Range size,
            @Nonnull Material replacement,
            @Nonnull World.Environment environment) {
        this.height = height;
        this.most = most;
        this.amount = amount;
        if (this.amount > 200) {
            this.amount = 200;
        }

        this.size = size;
        this.replacement = replacement;
        this.environment = environment;
    }

    public GenerationArea(@Nonnull Range height, int most, int amount, @Nonnull Range size) {
        this(height, most, amount, size, Material.STONE, World.Environment.NORMAL);
    }
}
