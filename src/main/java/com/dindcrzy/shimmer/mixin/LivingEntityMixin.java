package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import com.dindcrzy.shimmer.ShimmerStatusAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ShimmerStatusAccessor {

    @Shadow @Nullable public abstract StatusEffectInstance getStatusEffect(StatusEffect effect);

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);

    @Shadow public abstract boolean removeStatusEffect(StatusEffect type);

    @Shadow public abstract boolean collides();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    
    private boolean wasPhasing = false;
    @Override
    public boolean wasShimmering() {
        return wasPhasing;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (!damageSource.isOutOfWorld() && hasStatusEffect(ModInit.PHASING)) {
            return true;
        }
        return super.isInvulnerableTo(damageSource);
    }

    // overrides all movement when phasing
    @Override
    public void move(MovementType movementType, Vec3d movement) {
        StatusEffectInstance phasing = getStatusEffect(ModInit.PHASING);
        if (phasing != null && !isSpectator()) {
            wasPhasing = true;
            setVelocity(0, 0, 0);
            fallDistance = 0;
            double speed = Math.min(phasing.getAmplifier() * 0.1d + 0.1, 0.5d);
            Vec3d vel = new Vec3d(0, -speed, 0);
            
            if (phasing.getDuration() < 3 && Helper.isInBlock(this, 0.05d)) {
                removeStatusEffect(ModInit.PHASING);
                addStatusEffect(new StatusEffectInstance(
                        ModInit.PHASING, 
                        3, 
                        phasing.getAmplifier(), 
                        phasing.isAmbient(), 
                        phasing.shouldShowParticles()
                ));
            }
            
            setPosition(getPos().add(vel));
        } else {
            super.move(movementType, movement);
            if (isPlayer() && !isSpectator() && Helper.isInFluid(this, ModInit.STILL_SHIMMER, 0.2)) {
                addStatusEffect(new StatusEffectInstance(
                        ModInit.PHASING,
                        3,
                        0,
                        true,
                        false
                ));
            }
        }
    }

    @Override
    public void onLanding() {
        super.onLanding();
        wasPhasing = false;
    }
}
