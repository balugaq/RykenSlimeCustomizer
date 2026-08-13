package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WorkBenchMachineTicker extends LinkedRecipeMachineTicker {
    @Override
    default Type getType() {
        return Type.WORKBENCH;
    }

    @Override
    default boolean preTick(Location location) {
        return true;
    }

    @Override
    default void tick(Location location) {
        // empty ticker
    }
}
