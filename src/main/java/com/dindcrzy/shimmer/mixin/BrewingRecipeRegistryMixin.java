package com.dindcrzy.shimmer.mixin;

import com.dindcrzy.shimmer.ModInit;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {
    @Shadow
    private static void registerPotionRecipe(Potion input, Item item, Potion output) {
    }
    
    @Inject(method = "registerDefaults", at = @At("TAIL"))
    private static void potionRecipes(CallbackInfo ci) {
        registerPotionRecipe(ModInit.PHASING_POTION, Items.FERMENTED_SPIDER_EYE, ModInit.AETHER_SIGHT_POTION);
        registerPotionRecipe(ModInit.AETHER_SIGHT_POTION, Items.GLOWSTONE_DUST, ModInit.STRONG_AETHER_SIGHT_POTION);
    }
}
