package com.dindcrzy.shimmer;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

// only the cloak will be implemented
public class ShimmerArmorMaterial implements ArmorMaterial {
    public static final ShimmerArmorMaterial INSTANCE = new ShimmerArmorMaterial();
    
    @Override
    public int getDurability(EquipmentSlot slot) {
        return 200;
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return 4;
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.PHANTOM_MEMBRANE);
    }

    @Override
    public String getName() {
        return "shimmer";
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }
}
