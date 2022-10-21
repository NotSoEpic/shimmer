package com.dindcrzy.shimmer.xray;

import com.dindcrzy.shimmer.ModInit;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// https://github.com/Leximon/Spelunker/blob/1.18/src/main/java/de/leximon/spelunker/core/SpelunkerEffectRenderer.java

// client side data store which handles drawing the ore outlines
public class Renderer {
    // Vec3i: chunk coordinate
    // ChunkOres: list of coordinates within chunk
    private final ConcurrentHashMap<Vec3i, ChunkOres> chunkSections = new ConcurrentHashMap<>();
    private boolean active = false;

    public boolean isActive() {
        return active;
    }
    
    public boolean setActive(boolean value) {
        boolean init = value && !active;
        active = value;
        // if going from inactive to active state
        return init;
    }
    
    public void removeChunk(Vec3i pos) {
        chunkSections.remove(pos);
    }

    public ChunkOres get(Vec3i pos) {
        return chunkSections.get(pos);
    }
    
    public void clear() {
        chunkSections.clear();
    }
    
    // i believe bottomSectionCoord is the lowest chunk height?
    public void addChunks(int bottomSectionCoord, Collection<ChunkOres> chunks) {
        for (ChunkOres chunk : chunks) {
            chunkSections.put(chunk.getChunkPos(), chunk.remapToBlockCoordinates(bottomSectionCoord));
        }
    }
    
    // removes given chunk regions and adds new ones
    public void updateChunks(World world, Collection<Vec3i> remove, Collection<ChunkOres> add) {
        for (Vec3i v : remove) chunkSections.remove(v);
        for (ChunkOres chunk : add) {
            chunkSections.put(chunk.getChunkPos(), chunk
                .remapToBlockCoordinates(world.getBottomSectionCoord()));
        }
    }

    // called every frame
    public void render(MatrixStack matrices, Camera camera, OutlineVertexConsumerProvider vertexConsumers) {
        Vec3d pos = camera.getPos();
        // matrix math D:
        matrices.push();
        matrices.translate(-pos.x, -pos.y, -pos.z);
        for (Map.Entry<Vec3i, ChunkOres> chunkSection : chunkSections.entrySet()) {
            renderChunk(chunkSection.getValue(), matrices, pos, vertexConsumers);
        }
        matrices.pop();
    }
    
    // shape of outline model for every block
    private static final ModelPart.Cuboid CUBE = new ModelPart.Cuboid(0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, false, 0, 0);
    private static final RenderLayer RENDER_LAYER = RenderLayer.getOutline(new Identifier(ModInit.MOD_ID, "textures/none.png"));
    
    public void renderChunk(ChunkOres chunk, MatrixStack matrices, Vec3d playerPos, OutlineVertexConsumerProvider vertexConsumers) {
        vertexConsumers.setColor(255, 255, 255, 255);
        for (Vec3i pos : chunk) {
            double squareDist = playerPos.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
            if (squareDist > 8 * 8) {
                continue;
            }
            // capped linear interpolate
            // dist 0 - 2 : fade = 1
            // dist 2 - 8 : fade = 1 - 0
            float fade = (float) Math.min(1 - ((squareDist - 2 * 2) / (8 * 8 - 2 * 2)), 1);
            matrices.push();
            matrices.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            matrices.scale(fade, fade, fade);
            matrices.push();
            matrices.translate(-0.5, -0.5, -0.5);
            CUBE.renderCuboid(matrices.peek(), vertexConsumers.getBuffer(RENDER_LAYER), 0, OverlayTexture.DEFAULT_UV, 0, 0, 0, 0);
            matrices.pop();
            matrices.pop();
        }
    }
}
