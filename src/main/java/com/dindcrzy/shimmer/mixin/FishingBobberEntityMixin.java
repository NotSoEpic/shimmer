package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import com.dindcrzy.shimmer.ShimmerStatusAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends Entity {
    @Shadow @Nullable public abstract PlayerEntity getPlayerOwner();

    public FishingBobberEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;refreshPosition()V"))
    private void phaseOwner(CallbackInfo ci) {
        if (Helper.isInFluid(this, ModInit.STILL_SHIMMER)) {
            PlayerEntity owner = getPlayerOwner();
            if (owner != null) {
                owner.addStatusEffect(new StatusEffectInstance(
                        ModInit.PHASING,
                        10,
                        0,
                        true,
                        false
                ));
                if (owner instanceof ShimmerStatusAccessor status) {
                    status.setWasShimmering(true);
                }
                discard();
            }
        }
    }
}
