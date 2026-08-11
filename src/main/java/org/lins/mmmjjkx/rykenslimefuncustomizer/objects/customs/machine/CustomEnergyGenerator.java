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
package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.machine;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import org.bukkit.Location;
import org.graalvm.polyglot.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.CustomMenu;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.machine.MachineRecord;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.script.ScriptEval;
import org.lins.mmmjjkx.rykenslimefuncustomizer.objects.yaml.YamlReader;
import org.lins.mmmjjkx.rykenslimefuncustomizer.super_multiblock.SuperMultiBlockManager;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Debug;

import java.util.List;

public class CustomEnergyGenerator extends CustomMachine implements EnergyNetProvider {
    private final ScriptEval eval;
    private final int defaultOutput;

    public CustomEnergyGenerator(
            YamlReader.BaseResult base,
            @Nullable CustomMenu menu,
            List<Integer> input,
            List<Integer> output,
            MachineRecord record,
            EnergyNetComponentType type,
            @Nullable ScriptEval eval,
            int defaultOutput) {
        super(base, menu, input, output, record, type, eval);

        this.eval = eval;
        this.defaultOutput = defaultOutput;
    }

    @Override
    public int getGeneratedOutput(@NonNull Location l, @NonNull SlimefunBlockData data) {
        if (!SuperMultiBlockManager.canTick(l)) return 0;
        if (eval == null) {
            return defaultOutput;
        } else {
            try {
                Value result = eval.evalFunction("getGeneratedOutput", l, data);
                if (result != null) {
                    return result.asInt();
                } else {
                    Debug.warn(
                            "getGeneratedOutput() 返回了一个非整数值: " + result + " 导致自定义发电机的默认输出值将被使用， 请找附属对应作者修复此问题！");
                    return defaultOutput;
                }
            } catch (Exception e) {
                return defaultOutput;
            }
        }
    }
}
