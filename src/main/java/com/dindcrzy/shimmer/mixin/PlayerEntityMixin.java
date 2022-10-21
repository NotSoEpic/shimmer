package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.ClientInit;
import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import com.dindcrzy.shimmer.xray.ChunkOres;
import com.dindcrzy.shimmer.xray.EffectManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isCreative();

    @Shadow public abstract void remove(RemovalReason reason);

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
    
    // https://github.com/Leximon/Spelunker/blob/1b878a3e1cc026e617244240ad9e26fa328450cf/src/main/java/de/leximon/spelunker/mixin/PlayerEntityMixin.java#L81
    
    private final HashSet<Vec3i> effectChunks = new HashSet<>();
    private int lastCx, lastCy, lastCz;
    private boolean forceUpdate = true;
    @Inject(method = "tick", at = @At("HEAD"))
    private void moveEndInject(CallbackInfo ci) {
        if (!EffectManager.showXRay(this)) {
            effectChunks.clear();
            // when player next receives effect, update
            // even if they didnt move to a new chunk
            forceUpdate = true;
        } else {
            int cx = ChunkSectionPos.getSectionCoord(getX());
            int cy = ChunkSectionPos.getSectionCoord(getY());
            int cz = ChunkSectionPos.getSectionCoord(getZ());
            
            // only updates chunk list if player moves to new chunk
            if (cx != lastCx || cy != lastCy || cz != lastCz || forceUpdate) {
                forceUpdate = false;
                HashMap<Vec3i, ChunkSection> newChunks = EffectManager.getSurroundingChunkSections(world, getPos());
                
                // removes chunk coordinates that will no longer be tracked from effectChunks
                // remove is the deleted differential
                HashSet<Vec3i> remove = new HashSet<>();
                effectChunks.removeIf(p -> {
                    if (!newChunks.containsKey(p)) {
                        remove.add(p);
                        return true;
                    }
                    return false;
                });
                // adds chunk coordinate that will now be tracked to effectChunks
                // add is the added differential
                ArrayList<ChunkOres> add = new ArrayList<>();
                for (Map.Entry<Vec3i, ChunkSection> section : newChunks.entrySet()) {
                    Vec3i pos = section.getKey();
                    if (!effectChunks.contains(pos)) {
                        add.add(EffectManager.findOresInChunk(world, pos));
                        effectChunks.add(pos);
                    }
                }

                if (world.isClient()) {
                    // client can direcly access renderer to update stuff
                    ClientInit.renderer.updateChunks(world, remove, add);
                } else {
                    // server needs to send packet to client to update stuff
                    PacketByteBuf buf = EffectManager.writePacket(world, true, remove, add);
                    ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, EffectManager.ORE_PACKET, buf);
                }
            }
            
            // updates last chunk position
            lastCx = cx;
            lastCy = cy;
            lastCz = cz;
        }
    }
}
