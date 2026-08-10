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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.machine;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.factories.SimpleMachineFactory;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.SimpleMachineType;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.List;

public class SimpleMachineReader extends YamlReader<SlimefunItem> {
    @Override
    public String getFileName() {
        return Constants.SIMPLE_MACHINES_FILE;
    }

    public SimpleMachineReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public SlimefunItem readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        var base = getBase(section, s);
        if (base == null) return null;

        String machineTypeStr = section.getString("type");

        Pair<ExceptionHandler.HandleResult, SimpleMachineType> machineTypePair = CommonUtils.getEnum(
                "错误的简单机器类型 " + machineTypeStr, SimpleMachineType.class, machineTypeStr);
        if (machineTypePair.getFirstValue() == ExceptionHandler.HandleResult.FAILED
                || machineTypePair.getSecondValue() == null) {
            return null;
        }

        SimpleMachineType machineType = machineTypePair.getSecondValue();
        ConfigurationSection settings = section.getConfigurationSection("settings");

        if (settings == null) {
            ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载简单机器" + s + "时遇到了问题: " + "机器没有设置");
            return null;
        }

        int capacity = 0;
        int consumption = 0;
        int speed = 1;
        int radius = 1;
        int repairFactor = 10;

        if (machineType.isEnergy()) {
            capacity = settings.getInt("capacity");
            if (capacity < 1) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载简单机器" + s + "时遇到了问题: " + "容量小于1");
                return null;
            }

            consumption = settings.getInt("consumption");
            if (consumption < 1) {
                ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载简单机器" + s + "时遇到了问题: " + "消耗能量小于1");
                return null;
            }

            if (!isAccelerator(machineType)) {
                speed = settings.getInt("speed", 1);
                if (speed < 1) {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载简单机器" + s + "时遇到了问题: " + "处理速度小于1");
                    return null;
                }
            } else {
                radius = settings.getInt("radius", 1);
                if (radius < 1) {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载简单机器" + s + "时遇到了问题: " + "范围小于1");
                    return null;
                }

                if (machineType == SimpleMachineType.CROP_GROWTH_ACCELERATOR) {
                    speed = settings.getInt("speed", 1);
                    if (speed < 1) {
                        ExceptionHandler.handleError(
                                "在附属" + addon.getAddonId() + "中加载简单机器" + s + "时遇到了问题: " + "处理速度小于1");
                        return null;
                    }
                }
            }

            if (machineType == SimpleMachineType.AUTO_ANVIL) {
                repairFactor = settings.getInt("repair_factor", 10);
                if (repairFactor < 1) {
                    ExceptionHandler.handleError("在附属" + addon.getAddonId() + "中加载简单机器" + s + "时遇到了问题: " + "修理因子小于1");
                    return null;
                }
            }
        }

        SlimefunItem instance = SimpleMachineFactory.create(
                base,
                machineType,
                capacity,
                consumption,
                speed,
                radius,
                repairFactor);

        instance.register(RykenSlimefunCustomizer.INSTANCE);

        return instance;
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return blockPreloadItems(s);
    }

    private boolean isAccelerator(SimpleMachineType type) {
        return type == SimpleMachineType.TREE_GROWTH_ACCELERATOR
                || type == SimpleMachineType.CROP_GROWTH_ACCELERATOR
                || type == SimpleMachineType.ANIMAL_GROWTH_ACCELERATOR;
    }
}
