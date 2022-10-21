package com.dindcrzy.shimmer.mixin.client;

import com.dindcrzy.shimmer.xray.IWorld;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ((IWorld) this).aetherUpdateChunks();
    }
}
