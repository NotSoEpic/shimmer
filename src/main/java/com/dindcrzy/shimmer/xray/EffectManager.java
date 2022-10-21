package com.dindcrzy.shimmer.xray;

import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import com.dindcrzy.shimmer.mixin.server.ThreadedAnvilChunkStorageAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

// https://github.com/Leximon/Spelunker/blob/1b878a3e1cc026e617244240ad9e26fa328450cf/src/main/java/de/leximon/spelunker/core/SpelunkerEffectManager.java#L118

// static functions to do stuff
public class EffectManager {
    public static final Identifier ORE_PACKET = new Identifier(ModInit.MOD_ID, "ore_packet");
    
    // gets cubic area of ChunkSections centered on playerPos
    public static HashMap<Vec3i, ChunkSection> getSurroundingChunkSections(World world, Vec3d playerPos) {
        int cx = ChunkSectionPos.getSectionCoord(playerPos.x);
        int cy = world.sectionCoordToIndex(ChunkSectionPos.getSectionCoord(playerPos.y));
        int cz = ChunkSectionPos.getSectionCoord(playerPos.z);
        
        HashMap<Vec3i, ChunkSection> sections = new HashMap<>();
        int chunkRadius = 1;
        for (int x = cx - chunkRadius; x <= cx + chunkRadius; x ++) {
            for (int y = cy - chunkRadius; y <= cy + chunkRadius; y ++) {
                for (int z = cz - chunkRadius; z <= cz + chunkRadius; z ++) {
                    WorldChunk chunk = world.getChunk(x, z);
                    ChunkSection[] sectionArray = chunk.getSectionArray();
                    if (y < 0 || y > sectionArray.length) continue;
                    sections.put(new Vec3i(x, y, z), sectionArray[y]);
                }
            }
        }
        return sections;
    }
    
    public static ChunkOres findOresInChunk(World world, Vec3i sectionPos) {
        Chunk chunk = null;
        if (world.getChunkManager().isChunkLoaded(sectionPos.getX(), sectionPos.getZ())) {
            if (world instanceof ServerWorld serverWorld) {
                ChunkHolder chunkHolder = ((ThreadedAnvilChunkStorageAccessor) serverWorld.getChunkManager().threadedAnvilChunkStorage)
                        .getChunkHolder2ElectricBoogaloo(ChunkPos.toLong(sectionPos.getX(), sectionPos.getZ()));
                if (chunkHolder != null) {
                    chunk = chunkHolder.getWorldChunk();
                }
            } else {
                chunk = world.getChunk(sectionPos.getX(), sectionPos.getZ(), ChunkStatus.FULL, false);
            }
        }
        if (chunk == null) return ChunkOres.EMPTY;
        ChunkSection section = chunk.getSection(sectionPos.getY());
        ChunkOres ores = new ChunkOres(sectionPos);
        var blockStates = section.getBlockStateContainer();
        // iterates over every block in the chunk (16*16*16 area)
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState block = blockStates.get(x, y, z);
                    if (isOre(block)) {
                        ores.add(new Vec3i(x, y, z));
                    }
                }
            }
        }
        return ores;
    }
    
    public static boolean isOre(BlockState state) {
        return state.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier(ModInit.MOD_ID, "aether_sight")));
    }
    
    // writes chunk data into packet to send to client
    public static PacketByteBuf writePacket(World world, boolean overwrite, Collection<Vec3i> remove, Collection<ChunkOres> add) {
        PacketByteBuf buf = PacketByteBufs.create();
        
        // if false, will try and merge data from add
        buf.writeBoolean(overwrite);
        
        buf.writeVarInt(remove.size());
        for (Vec3i pos : remove) {
            buf.writeVarInt(pos.getX());
            buf.writeVarInt(pos.getY());
            buf.writeVarInt(pos.getZ());
        }
        
        buf.writeVarInt(add.size());
        synchronized (ChunkOres.SYNCHRONIZER) {
            for (ChunkOres ores : add) {
                Vec3i pos = ores.getChunkPos();
                buf.writeVarInt(pos.getX());
                buf.writeVarInt(pos.getY());
                buf.writeVarInt(pos.getZ());
                
                buf.writeVarInt(ores.size());
                for (Vec3i orePos : ores) {
                    buf.writeByte(orePos.getX());
                    buf.writeByte(orePos.getY());
                    buf.writeByte(orePos.getZ());
                }
            }
        }
        // needs lowest world coordinate when overwriting positions
        if (overwrite) buf.writeVarInt(world.getBottomSectionCoord());
        
        return buf;
    }
    
    @Environment(EnvType.CLIENT)
    public static void readPacket(Renderer renderer, PacketByteBuf buf) {
        boolean overwrite = buf.readBoolean();
        int c = buf.readVarInt();
        for (int i = 0; i < c; i++) {
            renderer.removeChunk(new Vec3i(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            ));
        }
        
        int d = buf.readVarInt();
        ArrayList<ChunkOres> chunks = new ArrayList<>(d);
        for (int i = 0; i < d; i++) {
            Vec3i pos = new Vec3i(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt()
            );
            
            ChunkOres ores = overwrite ? new ChunkOres(pos) : renderer.get(pos);
            int e = buf.readVarInt();
            for (int j = 0; j < e; j++) {
                Vec3i orePos = new Vec3i(
                        buf.readByte(),
                        buf.readByte(),
                        buf.readByte()
                );
                ores.add(orePos);
            }
            if (overwrite) chunks.add(ores);
        }
        if (overwrite) {
            int bottomSectionCoord = buf.readVarInt();
            renderer.addChunks(bottomSectionCoord, chunks);
        }
    }
    
    // if player has level 3 aether sight (0 = level 1)
    public static boolean showXRay(LivingEntity player) {
        return Helper.hasStatus(player, ModInit.AETHER_SIGHT, 2);
    }
}
