package com.dindcrzy.shimmer;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

    public static boolean isPhasing(@Nullable PlayerEntity player) {
        if (player != null) {
            return player.hasStatusEffect(ModInit.PHASING);
        }   
        return false;
    }
}
