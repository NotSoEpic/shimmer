package com.dindcrzy.shimmer.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class AetherFeatureConfig implements FeatureConfig {
    public final BlockStateProvider fillingProvider;
    // i have no clue what a codec does
    // but it crashes if i dont have one
    // any way to either use this properly or make a more "dummy" version?
    public static final Codec<AetherFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BlockStateProvider.TYPE_CODEC.fieldOf("filling_provider").forGetter((config) -> {
            return config.fillingProvider;
        })).apply(instance, AetherFeatureConfig::new);
    });

    public AetherFeatureConfig(BlockStateProvider fillingProvider) {
        this.fillingProvider = fillingProvider;
    }
}
