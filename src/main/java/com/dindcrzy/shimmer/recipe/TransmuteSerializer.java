package com.dindcrzy.shimmer.recipe;

import com.dindcrzy.shimmer.ModInit;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
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
        int incount = 1;
        if (recipeJson.input.has("count")) {
            incount = recipeJson.input.get("count").getAsInt();
        }

        Ingredient input = Ingredient.fromJson(recipeJson.input);
        ArrayList<ItemStack> outputs = new ArrayList<>();
        for (JsonElement jsonElement : recipeJson.output) {
            String itemId;
            int itemCount = 1;
            NbtElement nbt = null;
            if (jsonElement instanceof JsonObject jsonObject) {
                itemId = jsonObject.get("item").getAsString();
                if (jsonObject.has("count")) {
                    itemCount = jsonObject.get("count").getAsInt();
                }
                if (jsonObject.has("nbt")) {
                    nbt = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, jsonObject.get("nbt"));
                }
            } else {
                itemId = jsonElement.getAsString();
            }
            // java wtf
            String finalItemId = itemId;
            Item outputItem = Registry.ITEM.getOrEmpty(new Identifier(itemId)).orElseThrow(
                    () -> new JsonSyntaxException("No such item " + finalItemId)
            );
            ItemStack output = new ItemStack(outputItem, itemCount);
            if (nbt != null) {
                if (nbt instanceof NbtCompound compound) {
                    output.setNbt(compound);
                } else {
                    ModInit.LOGGER.warn("Invalid output nbt for item " + itemId + " in recipe " + id.toString());
                }
            }
            outputs.add(output);
        }
        
        return new TransmuteRecipe(id, outputs, input, incount);
    }

    @Override
    // recipe -> packet
    public void write(PacketByteBuf buf, TransmuteRecipe recipe) {
        recipe.getInput().write(buf);
        buf.writeInt(recipe.getInputCount());
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
        int input_count = buf.readInt();
        ArrayList<ItemStack> outputs = new ArrayList<>();
        int length = buf.readByte();
        for (int _i = 0; _i < length; _i++) {
            ItemStack output = buf.readItemStack();
            outputs.add(output);
        }
        return new TransmuteRecipe(id, outputs, input, input_count);
    }
}
