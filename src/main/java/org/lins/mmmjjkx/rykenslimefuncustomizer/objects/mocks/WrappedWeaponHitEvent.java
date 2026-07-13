package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.mocks;

import com.google.common.base.Function;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

@SuppressWarnings({"deprecation", "unchecked"})
public class WrappedWeaponHitEvent extends EntityDamageByEntityEvent {
    private static final Field MODIFIERS_FILED;
    private static final Field MODIFIER_FUNCTIONS_FILED;

    static {
        try {
            MODIFIERS_FILED = EntityDamageEvent.class.getDeclaredField("modifiers");
            MODIFIERS_FILED.setAccessible(true);

            MODIFIER_FUNCTIONS_FILED = EntityDamageEvent.class.getDeclaredField("modifierFunctions");
            MODIFIER_FUNCTIONS_FILED.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final Entity damager;
    private final Entity damagee;

    public WrappedWeaponHitEvent(@NotNull Entity damager, @NotNull Entity damagee, @NotNull DamageCause cause, double damage) {
        super(damager, damagee, cause, damage);

        this.damager = MockObject.mockOne(damager);
        this.damagee = MockObject.mockOne(damagee);
    }

    public WrappedWeaponHitEvent(@NotNull Entity damager, @NotNull Entity damagee, @NotNull DamageCause cause, @NotNull Map<DamageModifier, Double> modifiers, @NotNull Map<DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions) {
        super(damager, damagee, cause, modifiers, modifierFunctions);

        this.damager = MockObject.mockOne(damager);
        this.damagee = MockObject.mockOne(damagee);
    }

    public WrappedWeaponHitEvent(@NotNull Entity damager, @NotNull Entity damagee, @NotNull DamageCause cause, @NotNull Map<DamageModifier, Double> modifiers, @NotNull Map<DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions, boolean critical) {
        super(damager, damagee, cause, modifiers, modifierFunctions, critical);

        this.damager = MockObject.mockOne(damager);
        this.damagee = MockObject.mockOne(damagee);
    }

    public WrappedWeaponHitEvent(@NotNull EntityDamageByEntityEvent originalEvent) throws IllegalAccessException {
        super(originalEvent.getDamager(), originalEvent.getEntity(), originalEvent.getCause(), (Map<DamageModifier, Double>) MODIFIERS_FILED.get(originalEvent), (Map<DamageModifier, ? extends Function<? super Double, Double>>) MODIFIER_FUNCTIONS_FILED.get(originalEvent), originalEvent.isCritical());

        this.damager = MockObject.mockOne(originalEvent.getDamager());
        this.damagee = MockObject.mockOne(originalEvent.getEntity());
    }

    @Override
    public @NotNull Entity getEntity() {
        return damagee;
    }

    @Override
    public @NotNull Entity getDamager() {
        return damager;
    }

    @Override
    public boolean callEvent() {
        throw new UnsupportedOperationException();
    }
}
