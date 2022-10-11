package com.dindcrzy.shimmer;

import com.dindcrzy.shimmer.recipe.TransmuteRecipe;
import com.dindcrzy.shimmer.recipe.TransmuteSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManagerLoader;
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
	public static final StatusEffect PHASING = new PhasingDebuff();

	public static Tag<Fluid> SHIMMER_TAG;

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
				new BucketItem(STILL_SHIMMER, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1))
		);
		Registry.register(Registry.STATUS_EFFECT, new Identifier(MOD_ID, "phasing"), PHASING);
	}
	private void initialiseNerdStuff() {
		// 🤓
		Registry.register(Registry.RECIPE_SERIALIZER,
				TransmuteSerializer.ID,
				TransmuteSerializer.INSTANCE);
		Registry.register(Registry.RECIPE_TYPE,
				new Identifier(MOD_ID, TransmuteRecipe.Type.ID), TransmuteRecipe.Type.INSTANCE);
	}
}
