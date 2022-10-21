package com.dindcrzy.shimmer.xray;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

// i dont know why its structured like this but it works and i dont dare change it
public interface IWorld {
    void aetherUpdateBlock(BlockPos pos, BlockState oldBlock, BlockState newBlock);
    
    void aetherUpdateChunks();
}
