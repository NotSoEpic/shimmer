package com.dindcrzy.shimmer;

import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;

public class ClientInit implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        fluidRendering();
        shaderStuff();
    }
    
    private void fluidRendering() {
        FluidRenderHandlerRegistry.INSTANCE.register(
                ModInit.STILL_SHIMMER,
                ModInit.FLOWING_SHIMMER,
                new SimpleFluidRenderHandler(
                        new Identifier("shimmer:block/shimmer_still"),
                        new Identifier("shimmer:block/shimmer_flowing"),
                        0xFADCF5
                )
        );

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), ModInit.STILL_SHIMMER, ModInit.FLOWING_SHIMMER);
        
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier("shimmer:block/shimmer_still"));
            registry.register(new Identifier("shimmer:block/shimmer_flowing"));
        });
    }

    // https://github.com/Ladysnake/Satin/blob/master/test_mods/depth/src/main/java/ladysnake/satindepthtest/SatinDepthTest.java
    private final MinecraftClient mc = MinecraftClient.getInstance();
    final ManagedShaderEffect STARFIELD = ShaderEffectManager.getInstance()
            .manage(new Identifier(ModInit.MOD_ID, "shaders/post/starfield.json"), shader -> {
                shader.setSamplerUniform("DepthSampler", ((ReadableDepthFramebuffer)mc.getFramebuffer()).getStillDepthMap());
            });
    
    private final Uniform1f uniformSTime = STARFIELD.findUniform1f("STime");
    private final Uniform4f cameraRotation = STARFIELD.findUniform4f("CameraRotation");
    private final Uniform1f strengthF = STARFIELD.findUniform1f("Strength");
    private int ticks;
    private double strength;

    private void shaderStuff() {
        ShaderEffectRenderCallback.EVENT.register(STARFIELD::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticks ++;
            if (Helper.isPhasing(mc.player)) {
                strength = Math.min(strength + 0.03, 1);
            } else {
                strength = Math.max(strength - 0.05, 0);
            }
        });
        PostWorldRenderCallback.EVENT.register((camera, tickDelta, nanoTime) -> {
            // passing in a time variable for "shifting" effect
            uniformSTime.set((ticks + tickDelta) / 20f);
            // i am not looking forwards to quaternion math
            Quaternion rotation = camera.getRotation();
            cameraRotation.set(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW());
            // how "strong" the effect is: increases when player is shimmering, decreases when not
            strengthF.set((float) strength);
        });
    }
}
