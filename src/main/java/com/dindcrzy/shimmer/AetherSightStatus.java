package com.dindcrzy.shimmer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class AetherSightStatus extends StatusEffect {
    protected AetherSightStatus() {
        super(StatusEffectCategory.NEUTRAL, 0x000000);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {}
}
