package com.dindcrzy.shimmer.mixin.client;

import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.SynchronousResourceReloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements SynchronousResourceReloader, AutoCloseable {
    @Shadow public abstract MinecraftClient getClient();

    // hides block outline when phasing
    @Inject(method = "shouldRenderBlockOutline", at = @At("RETURN"), cancellable = true)
    private void blockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (Helper.hasStatus(getClient().player, ModInit.PHASING)) {
            cir.setReturnValue(false);
        }
    }
}
