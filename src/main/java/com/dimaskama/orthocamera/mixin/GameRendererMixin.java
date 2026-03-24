package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.mojang.blaze3d.ProjectionType;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {

    @ModifyArg(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"
            ),
            index = 1
    )
    private ProjectionType modifyProjType(ProjectionType original) {
        if (OrthoCamera.isEnabled()) {
            return ProjectionType.ORTHOGRAPHIC;
        }
        return original;
    }

}
