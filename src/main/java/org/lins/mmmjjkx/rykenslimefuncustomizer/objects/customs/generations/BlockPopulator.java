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

import com.destroystokyo.paper.profile.PlayerProfile;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import java.net.URL;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.Range;

@SuppressWarnings("deprecation")
public class BlockPopulator extends org.bukkit.generator.BlockPopulator {
    private static final List<String> blockedWorlds = List.of(
            "CAsteroidBelt",
            "CMars",
            "CMoon",
            "dimensionalhome",
            "ft_world",
            "ne_muspelheim",
            "ne_niflheim",
            "SmallSpace",
            "space",
            "world_galactifun_earth_orbit",
            "world_galactifun_enceladus",
            "world_galactifun_europa",
            "world_galactifun_io",
            "world_galactifun_mars",
            "world_galactifun_the_moon",
            "world_galactifun_titan",
            "world_galactifun_venus",
            "world_void",
            "corporate_dimension",
            "logispace");

    @Override
    public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk source) {
        if (blockedWorlds.contains(world.getName())) {
            return;
        }

        List<ProjectAddon> addons = RykenSlimefunCustomizer.addonManager.getAllAddons();

        for (ProjectAddon addon : addons) {
            List<GenerationInfo> generationInfos = addon.getGenerationInfos();

            for (GenerationInfo generationInfo : generationInfos) {
                List<GenerationArea> areas = generationInfo.getAreas();

                for (GenerationArea area : areas) {
                    if (area.getEnvironment() != world.getEnvironment()) continue;

                    for (int i = 0; i < area.getAmount(); i++)
                        generateNext(source.getX(), source.getZ(), world, random, generationInfo, area);
                }
            }
        }
    }

    private void generateNext(
            int chunkX,
            int chunkZ,
            @Nonnull World world,
            @Nonnull Random random,
            @Nonnull GenerationInfo generationInfo,
            @Nonnull GenerationArea area) {
        Range height = area.getHeight();
        int h = height.getDistance() + 1;
        int r;

        if (h < 0) {
            h = 1;
        }

        double s2 = random.nextDouble(0, h);

        double sTop = (height.max() - area.getMost() + 1);
        if (s2 < sTop) {
            int h2MaxHeight = (int) (s2 * 2);
            r = height.max() - h2MaxHeight;
        } else {
            s2 -= sTop;
            int h2MinHeight = (int) (s2 * 2);
            r = height.min() + h2MinHeight;
        }

        int centerX = (chunkX << 4) + random.nextInt(16);
        int centerY = r;
        int centerZ = (chunkZ << 4) + random.nextInt(16);

        for (int i = 0; i < area.getSize().getRandomBetween(random); i++) {
            Location location = new Location(world, centerX, centerY, centerZ);
            Block block = world.getBlockAt(centerX, centerY, centerZ);
            if (!(centerX >= (chunkX << 4)
                    && centerX < (chunkX << 4) + 16
                    && centerZ >= (chunkZ << 4)
                    && centerZ < (chunkZ << 4) + 16)) {
                break;
            }
            if (block.getType() != area.getReplacement()) break;

            SlimefunItemStack slimefunItemStack = generationInfo.getSlimefunItemStack();

            block.setType(slimefunItemStack.getType(), false);
            if (slimefunItemStack.getType() == Material.PLAYER_HEAD) {
                SkullMeta meta = (SkullMeta) slimefunItemStack.getItemMeta();
                PlayerProfile profile = meta.getPlayerProfile();
                if (profile != null) {
                    PlayerTextures textures = profile.getTextures();
                    URL skin = textures.getSkin();
                    if (skin != null) {
                        PlayerHead.setSkin(block, PlayerSkin.fromURL(skin.toString()), false);
                    }
                }
            }

            BlockDataController controller = Slimefun.getDatabaseManager().getBlockDataController();
            controller.createBlock(
                    location, generationInfo.getSlimefunItemStack().getItemId());

            r = random.nextInt(0, 3);
            if (r == 0) {
                centerX++;
            } else if (r == 1) {
                centerY++;
            } else if (r == 2) {
                centerZ++;
            }
        }
    }
}
