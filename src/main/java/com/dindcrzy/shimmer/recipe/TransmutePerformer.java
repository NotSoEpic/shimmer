package com.dindcrzy.shimmer.recipe;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransmutePerformer {
    public record ShimmerTestResult(int cost, TransmuteRecipe match) {}
    public record UncraftTestResult(int cost, CraftingRecipe match) {}
    public record ResultCount(int stackReduction, HashMap<Item, Integer> result) {}
    public record RecipeOutput(int stackReduction, ArrayList<ItemStack> results) {}
    
    public static RecipeOutput getResults(ItemStack input, World world) {
        ShimmerTestResult recipeResult = findRecipe(input, world);
        int cost = recipeResult.cost;
        TransmuteRecipe recipe = recipeResult.match;

        int reduction = 0;
        HashMap<Item, Integer> resultMap = new HashMap<>();
        if (recipe != null) {
            // found specified shimmering recipe
            ResultCount countResult = getResultCount(cost, input.getCount(), recipe);
            reduction = countResult.stackReduction;
            resultMap = countResult.result;
        } else {
            // try and find crafting recipe
        }
        
        ArrayList<ItemStack> results = itemiseOutputs(resultMap);
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
    
    // todo: use mega brain powers to actually finish this
    /*public static UncraftTestResult findUncrafting(ItemStack input, World world) {
        int cost = 0;
        CraftingRecipe cachedMatch = null;
        while(cachedMatch == null && cost++ <= input.getCount()) {
            world.getRecipeManager().listAllOfType(CraftingRecipe.Type)
        }
        
        return new UncraftTestResult(cost, cachedMatch);
    }*/
    
    // counts how many of each item should be gotten from the recipe
    public static ResultCount getResultCount(int cost, int inputCount, TransmuteRecipe recipe) {
        HashMap<Item, Integer> resultMap = new HashMap<>();
        int reduction = 0;
        if (cost > 0) {
            for (int i = cost; i <= inputCount; i += cost) {
                reduction += cost;
                for (ItemStack itemStack : recipe.getOutputs()) {
                    int count = resultMap.getOrDefault(itemStack.getItem(), 0);
                    count += itemStack.getCount();
                    resultMap.put(itemStack.getItem(), count);
                }
            }
        }
        
        return new ResultCount(reduction, resultMap);
    }
    
    // gets realistic itemstacks for output counts
    public static ArrayList<ItemStack> itemiseOutputs(HashMap<Item, Integer> resultCount) {
        ArrayList<ItemStack> results = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : resultCount.entrySet()) {
            Item item = entry.getKey();
            Integer count = entry.getValue();
            while (count > 0) {
                ItemStack stack = item.getDefaultStack();
                stack.setCount(Math.min(item.getMaxCount(), count));

                results.add(stack);
                count -= item.getMaxCount();
            }
        }

        return results;
    }
}
