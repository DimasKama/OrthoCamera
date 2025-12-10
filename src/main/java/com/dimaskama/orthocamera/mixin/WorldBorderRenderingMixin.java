package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.state.WorldBorderRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorderRenderer.class)
public class WorldBorderRenderingMixin {

    @Inject(
            method = "render",
            at = @At("HEAD"),
            cancellable = true
    )
    private void render(WorldBorderRenderState state, Vec3 cameraPos, double viewDistanceBlocks, double farPlaneDistance, CallbackInfo ci) {
        if (!OrthoCamera.isEnabled() || !OrthoCamera.CONFIG.hide_world_border) {
            return;
        }

        ci.cancel();
    }

}