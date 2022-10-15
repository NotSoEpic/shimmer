package com.dindcrzy.shimmer.recipe;

import com.dindcrzy.shimmer.Helper;
import com.dindcrzy.shimmer.ModInit;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransmutePerformer {
    public record ResultCount(int stackReduction, HashMap<ItemStack, Integer> result) {}
    public record RecipeOutput(int stackReduction, ArrayList<ItemStack> results) {}
    
    public static RecipeOutput getResults(ItemStack input, World world) {
        TransmuteRecipe recipeResult = findRecipe(input, world);

        int reduction = 0;
        HashMap<ItemStack, Integer> resultMap = new HashMap<>();
        ResultCount countResult = null;
        if (recipeResult != null) {
            // found specified shimmering recipe
            countResult = getResultCount(input, recipeResult);
        }
        if (countResult == null && Helper.canUncraft(input)) {
            // didnt find specific shimmering recipe
            CraftingRecipe uncraftingRecipe = findUncrafting(input, world);
            if (uncraftingRecipe != null) {
                // found crafting recipe
                countResult = getResultCount(input, uncraftingRecipe);
            }
            if (countResult == null) {
                SmeltingRecipe unsmeltingRecipe = findUnsmelting(input, world);
                if (unsmeltingRecipe != null) {
                    // found smelting recipe
                    countResult = getResultCount(input, unsmeltingRecipe);
                }
            }
        }
        
        if (countResult != null) {
            reduction = countResult.stackReduction;
            resultMap = countResult.result;
        }
        ArrayList<ItemStack> results = mergeOutputs(resultMap);
        return new RecipeOutput(reduction, results);
    }
    
    // finds a matching recipe for the input
    public static TransmuteRecipe findRecipe(ItemStack input, World world) {
        List<TransmuteRecipe> transmuteRecipes = world.getRecipeManager().listAllOfType(ModInit.TRANSMUTE_RECIPE);
        for (TransmuteRecipe recipe : transmuteRecipes) {
            if (recipe.matches(new SimpleInventory(input), world) && input.getCount() >= recipe.getInputCount()) {
                return recipe;
            }
        }
        return null;
    }
    
    public static CraftingRecipe findUncrafting(ItemStack input, World world) {
        List<CraftingRecipe> craftingRecipes = world.getRecipeManager().listAllOfType(RecipeType.CRAFTING);
        for (CraftingRecipe recipe : craftingRecipes) {
            if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
                ItemStack out = recipe.getOutput();
                if (input.getItem() == out.getItem() &&
                        out.getCount() <= input.getCount() &&
                        !Helper.hasInvalidUncraftingStack(recipe)) {
                    return recipe;
                }
            }
        }
        
        return null;
    }

    public static SmeltingRecipe findUnsmelting(ItemStack input, World world) {
        List<SmeltingRecipe> craftingRecipes = world.getRecipeManager().listAllOfType(RecipeType.SMELTING);
        for (SmeltingRecipe recipe : craftingRecipes) {
            ItemStack out = recipe.getOutput();
            if (input.getItem() == out.getItem() &&
                    out.getCount() <= input.getCount() &&
                    !Helper.hasInvalidUncraftingStack(recipe)) {
                return recipe;
            }
        }

        return null;
    }
    
    // returns list of itemstacks to produce
    public static ResultCount getResultCount(ItemStack input, TransmuteRecipe recipe) {
        int craftCount = Math.floorDiv(input.getCount(), recipe.getInputCount());
        HashMap<ItemStack, Integer> resultMap = new HashMap<>();
        for (ItemStack itemStack : recipe.getOutputs()) {
            int count = resultMap.getOrDefault(itemStack, 0);
            count += itemStack.getCount() * craftCount;
            resultMap.put(itemStack, count);
        }
        
        return new ResultCount(craftCount * recipe.getInputCount(), resultMap);
    }
    public static ResultCount getResultCount(ItemStack input, CraftingRecipe recipe) {
        int craftCount = Math.floorDiv(input.getCount(), recipe.getOutput().getCount());
        HashMap<ItemStack, Integer> resultMap = new HashMap<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ItemStack itemStack = Helper.getValidUncraftingStack(ingredient);
            if (itemStack != null) {
                int count = resultMap.getOrDefault(itemStack, 0);
                count += itemStack.getCount() * craftCount;
                resultMap.put(itemStack, count);
            }
        }
        if (resultMap.isEmpty()) {
            return null;
        } else {
            return new ResultCount(craftCount * recipe.getOutput().getCount(), resultMap);
        }
    }
    public static ResultCount getResultCount(ItemStack input, SmeltingRecipe recipe) {
        int craftCount = Math.floorDiv(input.getCount(), recipe.getOutput().getCount());
        // smelting recipes are always 1 input
        HashMap<ItemStack, Integer> resultMap = new HashMap<>();
        resultMap.put(Helper.getValidUncraftingStack(recipe.getIngredients().get(0)), craftCount);
        if (resultMap.isEmpty()) {
            return null;
        } else {
            return new ResultCount(craftCount * recipe.getOutput().getCount(), resultMap);
        }
    }
    
    // gets realistic itemstacks for output counts
    public static ArrayList<ItemStack> mergeOutputs(HashMap<ItemStack, Integer> resultCount) {
        ArrayList<ItemStack> results = new ArrayList<>();
        for (Map.Entry<ItemStack, Integer> entry : resultCount.entrySet()) {
            ItemStack item = entry.getKey();
            Integer count = entry.getValue();
            while (count > 0) {
                ItemStack stack = item.copy();
                stack.setCount(Math.min(item.getMaxCount(), count));

                results.add(stack);
                count -= item.getMaxCount();
            }
        }

        return results;
    }
}
