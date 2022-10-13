package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isCreative();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Inject(method = "canModifyBlocks", at = @At("RETURN"), cancellable = true)
    private void canModifyBlocks(CallbackInfoReturnable<Boolean> cir) {
        if (Helper.hasStatus(this, ModInit.PHASING) && !isCreative()) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (Helper.hasStatus(this, ModInit.PHASING) && !isCreative()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
    
    // doesnt work :/
    @Inject(method = "canConsume", at = @At("RETURN"), cancellable = true)
    private void canConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        if (Helper.hasStatus(this, ModInit.PHASING) && !isCreative()) {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = "tick", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;updatePose()V"))
    private void pose(CallbackInfo ci) {
        if (Helper.hasStatus(this, ModInit.PHASING)) {
            setPose(EntityPose.STANDING);
        }
    }
}
