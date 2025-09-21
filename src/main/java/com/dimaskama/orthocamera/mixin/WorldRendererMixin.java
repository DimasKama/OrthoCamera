package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(
            method = "renderWorldBorder",
            at = @At("HEAD"),
            cancellable = true
    )
    private void render(Camera camera, CallbackInfo ci) {
        if (!OrthoCamera.isEnabled() || !OrthoCamera.CONFIG.hide_world_border) {
            return;
        }

        ci.cancel();
    }

}