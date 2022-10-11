package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.ShimmerStatusAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {
    @Shadow @Final private LivingEntity entity;

    @Shadow @Nullable private String fallDeathSuffix;

    @Shadow @Final private List<DamageRecord> recentDamage;

    @Inject(method = "setFallDeathSuffix", at = @At("TAIL"))
    private void shimmerFallDeath(CallbackInfo ci) {
        if (entity instanceof ShimmerStatusAccessor statusAccessor && statusAccessor.wasShimmering()) {
            fallDeathSuffix = "shimmer";
        }
    }
    
    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void voidPhase(CallbackInfoReturnable<Text> cir) {
        if (entity instanceof ShimmerStatusAccessor statusAccessor && statusAccessor.wasShimmering()
                && recentDamage.get(recentDamage.size() - 1).getDamageSource().isOutOfWorld()) {
            cir.setReturnValue(new TranslatableText("death.attack.outOfWorld.phase", entity.getDisplayName()));
        }
    }
}
