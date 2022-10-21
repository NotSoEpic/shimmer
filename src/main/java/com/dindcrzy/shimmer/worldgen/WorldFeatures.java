package com.dindcrzy.shimmer.worldgen;

import com.dindcrzy.shimmer.ModInit;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.Arrays;

// https://github.com/TheDeathlyCow/more-geodes/blob/1.18-update/src/main/java/com/github/thedeathlycow/moregeodes/features/ModFeatures.java
public class WorldFeatures {
    public static final AetherFeature AETHER_FEATURE = Registry.register(
            Registry.FEATURE, "aether", new AetherFeature(AetherFeatureConfig.CODEC));
    
    public static void registerFeatures() {
        completeFeatureRegistration("aether", AETHER_FEATURE, AETHER_FEATURE.getConfig(),
                RarityFilterPlacementModifier.of(36),
                SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(-16), YOffset.fixed(32)),
                BiomePlacementModifier.of()
        );
        BiomeModifications.addFeature(
                // all overworld biomes except ocean
                BiomeSelectors.foundInOverworld().and(BiomeSelectors.categories(Biome.Category.OCEAN).negate()),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                RegistryKey.of(Registry.PLACED_FEATURE_KEY, new Identifier(ModInit.MOD_ID, "aether"))
        );
    }
    
    private static <FC extends FeatureConfig> void completeFeatureRegistration(String name, Feature<FC> feature, FC featureConfig, PlacementModifier... placementModifiers) {
        ConfiguredFeature<? extends FeatureConfig, ?> configuredFeature = register(
                BuiltinRegistries.CONFIGURED_FEATURE, name, new ConfiguredFeature<>(feature, featureConfig)
        );
        register(BuiltinRegistries.PLACED_FEATURE, name, new PlacedFeature(RegistryEntry.of(configuredFeature), Arrays.stream(placementModifiers).toList()));
    }
    
    private static <V, T extends V> T register(Registry<V> registry, String name, T toRegister) {
        return Registry.register(registry, new Identifier(ModInit.MOD_ID, name), toRegister);
    }
}
