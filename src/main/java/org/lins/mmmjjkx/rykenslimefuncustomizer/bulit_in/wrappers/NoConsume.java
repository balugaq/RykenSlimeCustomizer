package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.wrappers;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;

@Getter
public class NoConsume {
    private int noConsumeAmountExcludeLinked = 0;
    private final IntSet linkedNoConsume = new IntOpenHashSet();

    public void addNoConsumeAmountExcludeLinked(int amount) {
        this.noConsumeAmountExcludeLinked += amount;
    }

    public void addLinkedNoConsume(int slot) {
        linkedNoConsume.add(slot);
    }
}
