package com.dindcrzy.shimmer.recipe;

import com.dindcrzy.shimmer.Helper;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.world.World;

import java.util.*;

public class TransmutePerformer {
    public record ShimmerTestResult(int cost, TransmuteRecipe match) {}
    public record UncraftTestResult(int cost, CraftingRecipe match) {}
    public record ResultCount(int stackReduction, HashMap<ItemStack, Integer> result) {}
    public record RecipeOutput(int stackReduction, ArrayList<ItemStack> results) {}
    
    public static RecipeOutput getResults(ItemStack input, World world) {
        ShimmerTestResult recipeResult = findRecipe(input, world);
        int cost = recipeResult.cost;
        TransmuteRecipe transmuteRecipe = recipeResult.match;

        int reduction = 0;
        HashMap<ItemStack, Integer> resultMap = new HashMap<>();
        if (transmuteRecipe != null) {
            // found specified shimmering recipe
            ResultCount countResult = getResultCount(cost, input.getCount(), transmuteRecipe);
            reduction = countResult.stackReduction;
            resultMap = countResult.result;
        } else if (Helper.canUncraft(input)) {
            UncraftTestResult uncraftTestResult = findUncrafting(input, world);
            CraftingRecipe uncraftingRecipe = uncraftTestResult.match;
            cost = uncraftTestResult.cost;
            if (uncraftingRecipe != null) {
                ResultCount countResult = getResultCount(cost, input.getCount(), uncraftingRecipe);
                reduction = countResult.stackReduction;
                resultMap = countResult.result;
            }
        }
        
        ArrayList<ItemStack> results = mergeOutputs(resultMap);
        return new RecipeOutput(reduction, results);
    }
    
    // finds a matching recipe for the input
    public static ShimmerTestResult findRecipe(ItemStack input, World world) {
        int cost = 0;
        TransmuteRecipe cachedMatch = null;
        while(cachedMatch == null && cost++ <= input.getCount()) {
            // tests item with registered recipes
            ItemStack testStack = input.copy();
            testStack.setCount(cost);
            SimpleInventory inv = new SimpleInventory(testStack);
            Optional<TransmuteRecipe> match = world.getRecipeManager()
                    .getFirstMatch(TransmuteRecipe.Type.INSTANCE, inv, world);
            if (match.isPresent()) {
                cachedMatch = match.get();
            }
        }
        return new ShimmerTestResult(cost, cachedMatch);
    }
    
    public static UncraftTestResult findUncrafting(ItemStack input, World world) {
        List<CraftingRecipe> craftingRecipes = world.getRecipeManager().listAllOfType(RecipeType.CRAFTING);
        for (CraftingRecipe recipe : craftingRecipes) {
            if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
                ItemStack out = recipe.getOutput();
                if (input.getItem() == out.getItem() &&
                        out.getCount() <= input.getCount()) {
                    return new UncraftTestResult(out.getCount(), recipe);
                }
            }
        }
        
        return new UncraftTestResult(0, null);
    }
    
    // returns list of itemstacks to produce
    public static ResultCount getResultCount(int cost, int inputCount, TransmuteRecipe recipe) {
        int craftCount = Math.floorDiv(inputCount, cost);
        HashMap<ItemStack, Integer> resultMap = new HashMap<>();
        for (ItemStack itemStack : recipe.getOutputs()) {
            int count = resultMap.getOrDefault(itemStack, 0);
            count += itemStack.getCount() * craftCount;
            resultMap.put(itemStack, count);
        }
        
        return new ResultCount(craftCount * cost, resultMap);
    }
    public static ResultCount getResultCount(int cost, int inputCount, CraftingRecipe recipe) {
        int craftCount = Math.floorDiv(inputCount, cost);
        HashMap<ItemStack, Integer> resultMap = new HashMap<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ItemStack itemStack = Helper.getValidUncraftingStack(ingredient);
            if (itemStack != null) {
                int count = resultMap.getOrDefault(itemStack, 0);
                count += itemStack.getCount() * craftCount;
                resultMap.put(itemStack, count);
            }
        }
        
        return new ResultCount(craftCount * cost, resultMap);
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
