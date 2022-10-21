package com.dindcrzy.shimmer.worldgen;

import com.dindcrzy.shimmer.ModInit;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.ChunkRandom;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AetherFeature extends Feature<AetherFeatureConfig> {
    private final AetherFeatureConfig FEATURE_CONFIG;
    private static final List<Block> amethystBlocks = List.of(
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.BUDDING_AMETHYST
    );

    public AetherFeature(Codec<AetherFeatureConfig> configCodec) {
        super(configCodec);
        FEATURE_CONFIG = new AetherFeatureConfig(BlockStateProvider.of(Blocks.OBSIDIAN));
    }

    // a lot of this code is taken from GeodeFeature
    @Override
    public boolean generate(FeatureContext<AetherFeatureConfig> context) {
        int pointNum = 4;
        int pointDist = 4;
        int pointOff = 2;
        double d = (double)pointNum / pointDist;
        double filling = 1D / Math.sqrt(1.7);
        double inner = 1D / Math.sqrt(2.2 + d);
        double middle = 1D / Math.sqrt(3.2D + d);
        double outer = 1D / Math.sqrt(4.2D + d);
        
        BlockPos origin = context.getOrigin();
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();
        ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(world.getSeed()));
        DoublePerlinNoiseSampler perlinNoiseSampler = DoublePerlinNoiseSampler.create(chunkRandom, -4, 1.0D);

        int lowest = 0;
        // picks random points to do math with idk
        List<Pair<BlockPos, Integer>> samplePoints = Lists.newLinkedList();
        for (int i = 0; i < pointNum; i++) {
            BlockPos pos = new BlockPos(random.nextInt(-pointDist, pointDist), random.nextInt(-pointDist, pointDist),random.nextInt(-pointDist, pointDist));
            samplePoints.add(Pair.of(pos, random.nextInt(1, pointOff)));
            lowest = Math.min(lowest, pos.getY());
        }
        double s;
        int size = 16;
        List<BlockPos> budding = new ArrayList<>();
        for (BlockPos bp : BlockPos.iterate(-size, -size, -size, size, size, size)) {
            s = 0;
            
            BlockPos pos = bp.add(origin);
            double v = perlinNoiseSampler.sample(pos.getX(), pos.getY(), pos.getZ());
            // does strange math based on random sample points
            for (Pair<BlockPos, Integer> pair : samplePoints) {
                s += MathHelper.fastInverseSqrt(bp.getSquaredDistance(pair.getFirst()) + pair.getSecond() + v);
            }
            // sets block based on s value
            if (s >= filling) {
                // core (air / shimmer)
                if (bp.getY() < lowest) {
                    setBlockState(world, pos, ModInit.SHIMMER.getDefaultState());
                } else {
                    setBlockState(world, pos, Blocks.AIR.getDefaultState());
                }
            } else if (s >= inner) {
                // inner layer (amethyst / budding amethyst)
                if (random.nextFloat() < 0.03) {
                    setBlockState(world, pos, Blocks.BUDDING_AMETHYST.getDefaultState());
                    // to add clusters later
                    budding.add(pos);
                } else {
                    setBlockState(world, pos, Blocks.AMETHYST_BLOCK.getDefaultState());
                }
            } else if (s >= middle) {
                // middle layer (calcite)
                setBlockState(world, pos, Blocks.CALCITE.getDefaultState());
            } else if (s >= outer) {
                // outer layer (smooth basalt)
                setBlockState(world, pos, Blocks.SMOOTH_BASALT.getDefaultState());
            }
        }
        // places amethyst buds on budding amethyst
        for (BlockPos bp : budding) {
            for (Direction dir : Direction.values()) {
                BlockState state = Util.getRandom(amethystBlocks, random).getDefaultState();
                if (state.contains(Properties.FACING)) {
                    state = state.with(Properties.FACING, dir);
                }
                BlockPos pos = bp.offset(dir);
                if (BuddingAmethystBlock.canGrowIn(world.getBlockState(pos))) {
                    setBlockState(world, pos, state);
                }
            }
        }
        return true;
    }
    
    public AetherFeatureConfig getConfig() {
        return FEATURE_CONFIG;
    }
}
