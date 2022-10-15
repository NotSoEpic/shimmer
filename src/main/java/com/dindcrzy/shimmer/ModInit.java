package com.dindcrzy.shimmer;

import com.dindcrzy.shimmer.recipe.TransmuteRecipe;
import com.dindcrzy.shimmer.recipe.TransmuteSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModInit implements ModInitializer {
	public static final String MOD_ID = "shimmer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static FlowableFluid STILL_SHIMMER;
	public static FlowableFluid FLOWING_SHIMMER;
	public static Block SHIMMER;
	public static Item SHIMMER_BUCKET;
	public static Item SHIMMER_CLOAK;
	public static final StatusEffect PHASING = new PhasingStatus();
	public static final Potion PHASING_POTION = new Potion(new StatusEffectInstance(PHASING, 40, 0, true, true));
	public static final StatusEffect AETHER_SIGHT = new AetherSightStatus();
	public static final Potion AETHER_SIGHT_POTION = new Potion(new StatusEffectInstance(AETHER_SIGHT, 20 * 60 * 8, 0, true, true));

	public static RecipeType<TransmuteRecipe> TRANSMUTE_RECIPE;
	
	@Override
	public void onInitialize() {
		initialiseContent();
		initialiseNerdStuff();
	}
	
	private void initialiseContent() {
		STILL_SHIMMER = Registry.register(
				Registry.FLUID,
				new Identifier(MOD_ID, "shimmer"),
				new ShimmerFluid.Still()
		);
		FLOWING_SHIMMER = Registry.register(
				Registry.FLUID,
				new Identifier(MOD_ID, "shimmer_flowing"),
				new ShimmerFluid.Flowing()
		);
		SHIMMER = Registry.register(
				Registry.BLOCK,
				new Identifier(MOD_ID, "shimmer"),
				new FluidBlock(STILL_SHIMMER, FabricBlockSettings.copy(Blocks.WATER).luminance((state) -> 15))
		);
		SHIMMER_BUCKET = Registry.register(
				Registry.ITEM,
				new Identifier(MOD_ID, "shimmer_bucket"),
				new BucketItem(STILL_SHIMMER, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).group(ItemGroup.MISC))
		);
		SHIMMER_CLOAK = Registry.register(Registry.ITEM,
				new Identifier(MOD_ID, "shimmer_cloak"),
				new ArmorItem(ShimmerArmorMaterial.INSTANCE, EquipmentSlot.CHEST, new Item.Settings().group(ItemGroup.COMBAT)));
		Registry.register(Registry.STATUS_EFFECT, new Identifier(MOD_ID, "phasing"), PHASING);
		Registry.register(Registry.POTION, new Identifier(MOD_ID, "phasing"), PHASING_POTION);
		Registry.register(Registry.STATUS_EFFECT, new Identifier(MOD_ID, "aether_sight"), AETHER_SIGHT);
		Registry.register(Registry.POTION, new Identifier(MOD_ID, "aether_sight"), AETHER_SIGHT_POTION);
	}
	private void initialiseNerdStuff() {
		// 🤓
		Registry.register(Registry.RECIPE_SERIALIZER,
				TransmuteSerializer.ID,
				TransmuteSerializer.INSTANCE);
		TRANSMUTE_RECIPE = Registry.register(Registry.RECIPE_TYPE,
				new Identifier(MOD_ID, TransmuteRecipe.Type.ID), TransmuteRecipe.Type.INSTANCE);
	}
}
