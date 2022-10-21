package com.dindcrzy.shimmer.xray;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;

import java.util.HashSet;

// stores coordinates of ores in a chunk section
public class ChunkOres extends HashSet<Vec3i> {
    // black magic tbh
    public static final Object SYNCHRONIZER = new Object();
    public static final ChunkOres EMPTY = new ChunkOres(Vec3i.ZERO);
    
    private final Vec3i chunkPos;
    // false -> local coordinates (0-15)
    // true -> global coordinates
    private boolean remapped = false;
    private int bottomSectionCoord;
    
    public ChunkOres(Vec3i chunkPos) {
        this.chunkPos = chunkPos;
    }
    
    public Vec3i getChunkPos() {
        return chunkPos;
    }

    @Override
    public boolean add(Vec3i vec3i) {
        synchronized (SYNCHRONIZER) {
            return super.add(vec3i);
        }
    }
    
    public ChunkOres remapToBlockCoordinates(int bottomSectionCoord) {
        this.remapped = true;
        this.bottomSectionCoord = bottomSectionCoord;
        HashSet<Vec3i> clone = new HashSet<>(this);
        clear();
        for (Vec3i pos : clone) {
            add(toBlockCoord(pos, this.chunkPos, bottomSectionCoord));
        }
        return this;
    }
    
    public void processPos(Vec3i pos, boolean localPos, boolean toAdd) {
        if (remapped && localPos) {
            pos = toBlockCoord(pos, chunkPos, bottomSectionCoord);
        } else if (!remapped && !localPos) {
            pos = toLocalCoord(pos);
        }
        if (toAdd) {
            this.add(pos);
        } else {
            this.remove(pos);
        }
    }
    
    public static Vec3i toLocalCoord(Vec3i blockPos) {
        return new Vec3i(
                ChunkSectionPos.getLocalCoord(blockPos.getX()),
                ChunkSectionPos.getLocalCoord(blockPos.getX()),
                ChunkSectionPos.getLocalCoord(blockPos.getX())
        );
    }

    public static Vec3i toBlockCoord(Vec3i localPos, Vec3i sectionPos, int bottomSectionCoord) {
        return new Vec3i(
                ChunkSectionPos.getBlockCoord(sectionPos.getX()) + localPos.getX(),
                ChunkSectionPos.getBlockCoord(sectionPos.getY() + bottomSectionCoord) + localPos.getY(),
                ChunkSectionPos.getBlockCoord(sectionPos.getZ()) + localPos.getZ()
        );
    }
}
