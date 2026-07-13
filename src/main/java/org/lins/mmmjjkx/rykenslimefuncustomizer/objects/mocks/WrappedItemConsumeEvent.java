package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.mocks;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class WrappedItemConsumeEvent extends PlayerItemConsumeEvent {
    public WrappedItemConsumeEvent(@NotNull Player player, @NotNull ItemStack item) {
        super(player, item);

        this.player = MockObject.mock(player);
    }

    public WrappedItemConsumeEvent(@NotNull Player player, @NotNull ItemStack item, @NotNull EquipmentSlot hand) {
        super(player, item, hand);

        this.player = MockObject.mock(player);
    }

    public WrappedItemConsumeEvent(@NotNull PlayerItemConsumeEvent originalEvent) {
        super(originalEvent.getPlayer(), originalEvent.getItem(), originalEvent.getHand());

        this.player = MockObject.mock(player);
    }

    @Override
    public boolean callEvent() {
        throw new UnsupportedOperationException();
    }
}
