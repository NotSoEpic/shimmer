package com.dindcrzy.shimmer.mixin.server;

import com.dindcrzy.shimmer.xray.IWorld;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    
    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    private void blockChange(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        ((IWorld) this).aetherUpdateBlock(pos, oldBlock, newBlock);
    }
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ((IWorld) this).aetherUpdateChunks();
    }
}
