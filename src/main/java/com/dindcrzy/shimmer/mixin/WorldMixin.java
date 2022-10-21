package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.ClientInit;
import com.dindcrzy.shimmer.xray.ChunkOres;
import com.dindcrzy.shimmer.xray.EffectManager;
import com.dindcrzy.shimmer.xray.IWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(World.class)
public abstract class WorldMixin implements IWorld, WorldAccess {
    
    @Shadow public abstract boolean isClient();

    @Shadow @Nullable public abstract MinecraftServer getServer();

    private final Map<Vec3i, ChunkOres> dirtyChunks = new ConcurrentHashMap<>();

    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    private void blockChange(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        // updates renderer instance with block change one way or the other
        aetherUpdateBlock(pos, oldBlock, newBlock);
    }
    
    @Override
    public void aetherUpdateBlock(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
        boolean oreAdd = EffectManager.isOre(newBlock);
        // does anything actually need to change?
        if (EffectManager.isOre(oldBlock) || oreAdd) {
            Vec3i chunkPos = new Vec3i(
                    ChunkSectionPos.getSectionCoord(pos.getX()),
                    sectionCoordToIndex(ChunkSectionPos.getSectionCoord(pos.getY())),
                    ChunkSectionPos.getSectionCoord(pos.getZ())
            );
            if (isClient()) {
                // client can just access renderer directly
                updateBlockClient(chunkPos, pos, oreAdd);
            } else {
                // server needs to send packets to clients
                dirtyChunks.compute(chunkPos, (p, chunk) -> {
                    if (chunk == null) chunk = new ChunkOres(chunkPos);
                    chunk.add(ChunkOres.toLocalCoord(pos));
                    return chunk;
                });
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void updateBlockClient(Vec3i chunkPos, BlockPos pos, boolean add) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer() && EffectManager.showXRay(client.player)) {
            ChunkOres chunk = ClientInit.renderer.get(chunkPos);
            if (chunk != null) {
                chunk.processPos(pos, false, add);
            }
        }
    }

    @Override
    public void aetherUpdateChunks() {
        if (!dirtyChunks.isEmpty()) {
            // gets every player that has the xray effect
            Collection<ServerPlayerEntity> players = PlayerLookup.all(Objects.requireNonNull(getServer())).stream()
                    .filter(EffectManager::showXRay)
                    .toList();
            if (players.size() > 0) {
                PacketByteBuf buf = EffectManager.writePacket((World) (Object) this, false, Collections.emptyList(), dirtyChunks.values());
                for (ServerPlayerEntity p : players) {
                    // sends them the block update packet
                    ServerPlayNetworking.send(p, EffectManager.ORE_PACKET, buf);
                }
            }
        }
    }
}
