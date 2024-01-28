package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.record;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;

public record MachineInfo(BlockMenu blockMenu, SlimefunBlockData data, SlimefunItem machineItem,
                          Block block, int totalTicks, int progress, MachineProcessor<?> processor, MachineOperation operation) {
    public Slimefun getSlimefunPlugin() {
        return Slimefun.getPlugin(Slimefun.class);
    }
}