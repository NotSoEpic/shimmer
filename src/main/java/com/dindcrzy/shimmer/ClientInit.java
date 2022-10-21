package com.dindcrzy.shimmer;

import com.dindcrzy.shimmer.mixin.client.WorldRendererAccessor;
import com.dindcrzy.shimmer.xray.EffectManager;
import com.dindcrzy.shimmer.xray.Renderer;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform3f;
import ladysnake.satin.api.managed.uniform.Uniform4f;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
        spelunkerStuff();
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
            .manage(new Identifier(ModInit.MOD_ID, "shaders/post/starfield.json"), shader -> 
                    shader.setSamplerUniform(
                            "DepthSampler", 
                            ((ReadableDepthFramebuffer)mc.getFramebuffer()).getStillDepthMap()
                    )
            );
    
    private final Uniform1f uniformSTime = STARFIELD.findUniform1f("STime");
    private final Uniform4f camQuart = STARFIELD.findUniform4f("CamQuart");
    private final Uniform3f camPos = STARFIELD.findUniform3f("CamPos");
    private final Uniform1f strengthF = STARFIELD.findUniform1f("Strength");
    private int ticks;
    private double strength;

    private void shaderStuff() {
        ShaderEffectRenderCallback.EVENT.register(STARFIELD::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticks ++;
            float target = 0f;
            float speed = 0.02f;
            if (Helper.hasStatus(mc.player, ModInit.PHASING)) {
                target = 1f;
            } else if(Helper.hasStatus(mc.player, ModInit.AETHER_SIGHT)) {
                target = Math.min((Helper.getPotionAmplifier(mc.player, ModInit.AETHER_SIGHT) + 1) / 5f, 1);
                speed = 0.01f;
            } else if (mc.player instanceof ShimmerStatusAccessor accessor && accessor.wasShimmering()) {
                speed = 0.05f;
            }
            strength += Math.min(speed, Math.abs(target-strength)) * Math.signum(target - strength);
        });
        PostWorldRenderCallback.EVENT.register((camera, tickDelta, nanoTime) -> {
            // passing in a time variable for "shifting" effect
            uniformSTime.set((ticks + tickDelta) / 20f);
            // i am not looking forwards to quaternion math
            Quaternion rotationQ = camera.getRotation();
            camQuart.set(rotationQ.getX(), rotationQ.getY(), rotationQ.getZ(), rotationQ.getW());
            camPos.set((float)camera.getPos().x, (float)camera.getPos().y, (float)camera.getPos().z);
            // how "strong" the effect is: increases when player is shimmering, decreases when not
            strengthF.set((float) strength);
            
            // todo: pass in FOV somehow. Vanilla's _FOV doesn't work
            // also view bobbing isnt part of camera pos/rotation?
        });
    }
    
    // https://github.com/Leximon/Spelunker/blob/1b878a3e1cc026e617244240ad9e26fa328450cf/src/main/java/de/leximon/spelunker/SpelunkerModClient.java#L32
    
    public static Renderer renderer = new Renderer();
    public static boolean isAlreadyRenderingOutline = false;
    
    private void spelunkerStuff() {
        WorldRenderEvents.LAST.register(context -> {
            WorldRendererAccessor worldRenderer = (WorldRendererAccessor) context.worldRenderer();
            
            if (renderer.isActive()) {
                if (!isAlreadyRenderingOutline) { // stops stuff from happening ig
                    worldRenderer.getEntityOutlineShader().render(context.tickDelta());
                    mc.getFramebuffer().beginWrite(false);
                }
                renderer.render(context.matrixStack(), context.camera(), worldRenderer.getBufferBuilders().getOutlineVertexConsumers());
            }
            isAlreadyRenderingOutline = false;
        });
        
        ClientPlayNetworking.registerGlobalReceiver(
                EffectManager.ORE_PACKET, ((client, handler, buf, responseSender) -> 
                        EffectManager.readPacket(renderer, buf)));
    }
}
