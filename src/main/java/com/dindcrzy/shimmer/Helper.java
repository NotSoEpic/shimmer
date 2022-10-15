package com.dindcrzy.shimmer;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public class Helper {
    // mostly copied from Entity$updateMovementInFluid
    public static boolean isInFluid(Entity entity, Fluid fluid) {
        return isInFluid(entity, fluid, 0.001d);
    }
    public static boolean isInFluid(Entity entity, Fluid fluid, double contraction) {
        Box box = entity.getBoundingBox().contract(contraction);
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.ceil(box.maxY);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int p = i; p < j; ++p) {
            for(int q = k; q < l; ++q) {
                for(int r = m; r < n; ++r) {
                    mutable.set(p, q, r);
                    FluidState fluidState = entity.world.getFluidState(mutable);
                    if (fluid.matchesType(fluidState.getFluid())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static boolean isInBlock(Entity entity) {
        return isInBlock(entity, 0.001d);
    }
    public static boolean isInBlock(Entity entity, double contraction) {
        Box box = entity.getBoundingBox().contract(contraction);
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.ceil(box.maxY);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int p = i; p < j; ++p) {
            for(int q = k; q < l; ++q) {
                for(int r = m; r < n; ++r) {
                    mutable.set(p, q, r);
                    BlockState state = entity.world.getBlockState(mutable);
                    if (entity.collidesWithStateAtPos(mutable, state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasStatus(@Nullable LivingEntity player, StatusEffect status) {
        if (player != null) {
            return player.hasStatusEffect(status);
        }   
        return false;
    }
    public static int getPotionAmplifier(@Nullable LivingEntity player, StatusEffect status) {
        if (player != null) {
            StatusEffectInstance inst = player.getStatusEffect(status);
            if (inst != null) {
                return inst.getAmplifier();
            }
        }
        return 0;
    }
    
    public static boolean canUncraft(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return (
            !itemStack.hasNbt() && 
            !item.isDamageable() && 
            !itemStack.isIn(
                TagKey.of(Registry.ITEM.getKey(), 
                new Identifier(ModInit.MOD_ID, "dont_uncraft")))
        );
    }
    
    public static ItemStack getValidUncraftingStack(Ingredient ingredient) {
        for (ItemStack itemStack : ingredient.getMatchingStacks()) {
            Item item = itemStack.getItem();
            if (!item.hasRecipeRemainder()) {
                return itemStack;
            }
        }
        return null;
    }
}
