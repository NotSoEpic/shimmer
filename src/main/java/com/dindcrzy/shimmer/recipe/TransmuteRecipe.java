package com.dindcrzy.shimmer.recipe;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;

// https://github.com/natanfudge/fabric-docs/blob/master/newdocs/Modding-Tutorials/Crafting-Recipes/defining-custom-crafting-recipes.md
// I wish I could understand this
public class TransmuteRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;
    private final ArrayList<ItemStack> result;
    private final Ingredient input;
    private final int input_count;
    
    public TransmuteRecipe(Identifier id, ArrayList<ItemStack> result, Ingredient input, int input_count) {
        this.id = id;
        this.result = result;
        this.input = input;
        this.input_count = input_count;
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return this.input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack craft(SimpleInventory inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    public Ingredient getInput() {
        return this.input;
    }
    public int getInputCount() {return this.input_count; }

    // why is this mandatory?
    @Override
    public ItemStack getOutput() {
        return this.result.get(0);
    }
    
    public ArrayList<ItemStack> getOutputs() {
        return this.result;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TransmuteSerializer.INSTANCE;
    }
    
    public static class Type implements RecipeType<TransmuteRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        
        public static final String ID = "one_slot_recipe";
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }
}
