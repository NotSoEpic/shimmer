package com.dindcrzy.shimmer.mixin.client;

import com.dindcrzy.shimmer.ClientInit;
import com.dindcrzy.shimmer.xray.EffectManager;
import com.dindcrzy.shimmer.xray.Renderer;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        Renderer renderer = ClientInit.renderer;
        if (renderer.setActive(EffectManager.showXRay(this))) {
            // clears renderer cache when being re-enabled
            renderer.clear();
        }
    }
}
