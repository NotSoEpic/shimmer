package com.dindcrzy.shimmer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class PhasingDebuff extends StatusEffect {
    protected PhasingDebuff() {
        super(
                StatusEffectCategory.HARMFUL,
                0xFADCF5
        );
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.isSpectator()) {
            entity.removeStatusEffect(ModInit.PHASING);
        } else {
            entity.noClip = true;
        }
    }
}
