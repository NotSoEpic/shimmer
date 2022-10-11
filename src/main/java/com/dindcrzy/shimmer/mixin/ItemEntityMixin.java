package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import com.dindcrzy.shimmer.recipe.TransmutePerformer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    @Shadow public abstract ItemStack getStack();

    @Shadow public abstract void setPickupDelay(int pickupDelay);

    @Shadow @Nullable public abstract UUID getOwner();

    @Shadow @Nullable public abstract UUID getThrower();

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    
    private static final String wasTransKey = "wasTransmuted";
    
    private int transmute_timer = 0;
    
    private static final UUID shimmerOwner = UUID.nameUUIDFromBytes("shimmer transmutation".getBytes());

    @Inject(method = "tick", at = @At("HEAD"))
    private void transmutation(CallbackInfo ci) {
        if (Objects.equals(getThrower(), shimmerOwner)) {
            // float upwards
            if (Helper.isInFluid(this, ModInit.STILL_SHIMMER)) {
                setVelocity(0, 0.1, 0);
            } else {
                setVelocity(0, getVelocity().y * 0.95, 0);
            }
            // i have no clue what it does besides fixing velocity desync
            velocityDirty = true;
            setOnGround(false);
        } else if (Helper.isInFluid(this, ModInit.STILL_SHIMMER)) {
            transmute_timer += 1;
        } else {
            transmute_timer = 0;
        }
        if (transmute_timer == 10 && !world.isClient && !Objects.equals(getThrower(), shimmerOwner)) {
            TransmutePerformer.RecipeOutput resultRecord = TransmutePerformer.getResults(getStack(), world);
            ArrayList<ItemStack> resultItems = resultRecord.results();
            int reduction = resultRecord.stackReduction();
            getStack().decrement(reduction);

            for (int offset = 0; offset < resultItems.size(); offset++) {
                ItemStack item = resultItems.get(offset);
                double angle = Math.PI * 2.0 * offset / resultItems.size();
                Vec3d pos = getPos();
                if (resultItems.size() > 1) {
                    Vec3d posOffset = new Vec3d(Math.sin(angle), 0, Math.cos(angle)).multiply(0.3);
                    pos = pos.add(posOffset);
                }
                ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, item);
                entity.setThrower(shimmerOwner);
                entity.setNoGravity(true);
                entity.setVelocity(0, 0.1, 0);
                entity.setOnGround(false);
                world.spawnEntity(entity);
            }
        }
    }
}
