package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in.tickers;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface LinkedRecipeMachineTicker extends RecipeMachineTicker {
    @Override
    default Type getType() {
        return Type.LINKED_RECIPE;
    }
}
