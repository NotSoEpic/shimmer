package com.dindcrzy.shimmer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class ShimmerFluid extends FlowableFluid {
    //region Generic stuff
    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    protected boolean isInfinite() {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        // gets the block entity at the position
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        // drops relevant items
        Block.dropStacks(state, world, pos, blockEntity);
    }
    
    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        // whether to allow other fluids to "override" it by flowing into it
        return false;
    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        // ???
        // "Possibly related to the distance checks for flowing into nearby holes?"
        // - fabricmc.net/wiki
        return 4;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        // what it says on the tin
        return 1;
    }

    @Override
    public int getTickRate(WorldView world) {
        // lower number = faster
        // water 5
        // lava 30, 10 in nether
        return 20;
    }

    @Override
    protected float getBlastResistance() {
        // both lava and water uses this value
        return 100.0f;
    }
    //endregion


    @Override
    public Fluid getStill() {
        return ModInit.STILL_SHIMMER;
    }

    @Override
    public Fluid getFlowing() {
        return ModInit.FLOWING_SHIMMER;
    }

    @Override
    public Item getBucketItem() {
        return ModInit.SHIMMER_BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return ModInit.SHIMMER.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
    }
    
    public static class Flowing extends ShimmerFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }
    
    public static class Still extends ShimmerFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
