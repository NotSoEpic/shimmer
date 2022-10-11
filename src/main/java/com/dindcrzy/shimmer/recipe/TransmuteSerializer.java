package com.dindcrzy.shimmer.recipe;

import com.dindcrzy.shimmer.ModInit;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.Arrays;

public class TransmuteSerializer implements RecipeSerializer<TransmuteRecipe> {
    private TransmuteSerializer() {}
    public static final TransmuteSerializer INSTANCE = new TransmuteSerializer();
    
    public static final Identifier ID = new Identifier("shimmer:transmuting");
    
    @Override
    // json -> recipe
    public TransmuteRecipe read(Identifier id, JsonObject json) {
        TransmuteJsonFormat recipeJson = new Gson().fromJson(json, TransmuteJsonFormat.class);
        if (recipeJson.input == null || recipeJson.output == null) {
            throw new JsonSyntaxException("A required attribute is missing");
        }
        if (recipeJson.count <= 0) {
            recipeJson.count = 1;
        }

        Ingredient input = Ingredient.fromJson(recipeJson.input);
        ArrayList<ItemStack> outputs = new ArrayList<>();
        for (JsonElement jsonElement : recipeJson.output) {
            String itemId;
            int itemCount = 1;
            if (jsonElement instanceof JsonObject jsonObject) {
                itemId = jsonObject.get("item").getAsString();
                itemCount = jsonObject.get("count").getAsInt();
            } else {
                itemId = jsonElement.getAsString();
            }
            // java wtf
            String finalItemId = itemId;
            Item outputItem = Registry.ITEM.getOrEmpty(new Identifier(itemId)).orElseThrow(
                    () -> new JsonSyntaxException("No such item " + finalItemId)
            );
            ItemStack output = new ItemStack(outputItem, itemCount);
            outputs.add(output);
        }
        
        return new TransmuteRecipe(id, outputs, input);
    }

    @Override
    // recipe -> packet
    public void write(PacketByteBuf buf, TransmuteRecipe recipe) {
        recipe.getInput().write(buf);
        // length of outputs
        buf.writeByte(recipe.getOutputs().size());
        for (ItemStack item : recipe.getOutputs()) {
            buf.writeItemStack(item);
        }
    }

    @Override
    // packet -> recipe
    public TransmuteRecipe read(Identifier id, PacketByteBuf buf) {
        // read order must be same as write order!
        Ingredient input = Ingredient.fromPacket(buf);
        ArrayList<ItemStack> outputs = new ArrayList<>();
        int length = buf.readByte();
        for (int _i = 0; _i < length; _i++) {
            ItemStack output = buf.readItemStack();
            outputs.add(output);
        }
        return new TransmuteRecipe(id, outputs, input);
    }
}
