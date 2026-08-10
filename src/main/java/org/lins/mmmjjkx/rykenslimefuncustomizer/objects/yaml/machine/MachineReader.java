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

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.lins.mmmjjkx.rykenslimefuncustomizer.RykenSlimefunCustomizer;
import org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.JavaScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.ProjectAddon;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomEnergyGenerator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine.CustomNoEnergyMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.parent.AbstractEmptyMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.MachineRecord;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.CommonUtils;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.ExceptionHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MachineReader extends YamlReader<AbstractEmptyMachine<?>> {
    @Override
    public String getFileName() {
        return Constants.MACHINES_FILE;
    }

    public MachineReader(File file, ProjectAddon addon) {
        super(file, addon);
    }

    @Override
    public AbstractEmptyMachine<?> readEach(String s) {
        ConfigurationSection section = configuration.getConfigurationSection(s);
        if (section == null) return null;
        String id = getId(s);
        var base = getBase(section, s);
        if (base == null) return null;

        JavaScriptEval eval = getScriptOrNull(section, section.getString("script"));

        List<Integer> input = section.getIntegerList("input");
        List<Integer> output = section.getIntegerList("output");
        CustomMenu menu = CommonUtils.getIf(addon.getMenus(), m -> m.getId().equalsIgnoreCase(id));

        AbstractEmptyMachine<?> machine;
        CustomNoEnergyMachine defaultNoEnergyMachine = new CustomNoEnergyMachine(base, menu, input, output, eval, -1);

        if (section.contains("energy")) {
            ConfigurationSection energySettings = section.getConfigurationSection("energy");
            if (energySettings == null) {
                Debug.error(file, section, "缺少或配置错误 '能源设置' (energy)");
                return null;
            }
            int capacity = energySettings.getInt("capacity", -1);
            if (capacity < 1) {
                Debug.error(file, section, "缺少或配置错误 '电容量' (capacity)", 1, Integer.MAX_VALUE);
                return null;
            }
            MachineRecord record = new MachineRecord(capacity);
            String encType = energySettings.getString("type");
            Pair<ExceptionHandler.HandleResult, EnergyNetComponentType> enc = CommonUtils.getEnum(
                    "无法读取在附属" + addon.getAddonId() + "中的机器" + s + "的能源设置，已转为无电机器，原因: 错误的能源网络组件类型" + encType,
                    EnergyNetComponentType.class,
                    encType);
            if (enc.getFirstValue() == ExceptionHandler.HandleResult.FAILED) {
                return defaultNoEnergyMachine;
            }

            if (energySettings.contains("energyOutput")) {
                int energyOutput = section.getInt("energyOutput", -1);
                if (energyOutput < 1) {
                    Debug.error(file, section, "缺少或配置错误 '能源输出' (energyOutput)");
                    return null;
                } else {
                    machine = new CustomEnergyGenerator(base, menu, input, output, record, enc.getSecondValue(), eval, energyOutput);
                }
            } else {
                machine = new CustomMachine(base, menu, input, output, record, enc.getSecondValue(), eval);
            }
        } else {
            List<Integer> workSlots = new ArrayList<>();
            if (section.isInt("work")) {
                workSlots = Collections.singletonList(section.getInt("work", -1));
            } else if (section.isList("work")) {
                workSlots = section.getIntegerList("work");
            }

            machine = new CustomNoEnergyMachine(base, menu, input, output, eval, workSlots);
        }

        machine.register(RykenSlimefunCustomizer.INSTANCE);
        return machine;
    }

    @Override
    public List<SlimefunItemStack> preloadItems(String s) {
        return blockPreloadItems(s);
    }
}
