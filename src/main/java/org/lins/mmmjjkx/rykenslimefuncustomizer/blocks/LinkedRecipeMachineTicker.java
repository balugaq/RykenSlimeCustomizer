package org.lins.mmmjjkx.rykenslimefuncustomizer.blocks;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface LinkedRecipeMachineTicker extends RecipeMachineTicker {
    @Override
    default Type getType() {
        return Type.LINKED_RECIPE;
    }
}
