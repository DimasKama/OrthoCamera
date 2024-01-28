package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @ModifyVariable(
            method = "render",
            at = @At("HEAD"),
            index = 9,
            argsOnly = true
    )
    private Matrix4f orthoProjMat(Matrix4f projMat, @Local(argsOnly = true) float tickDelta) {
        if (OrthoCamera.isEnabled()) {
            Matrix4f mat = OrthoCamera.createOrthoMatrix(tickDelta, 0.0F);
            RenderSystem.setProjectionMatrix(mat, VertexSorter.BY_DISTANCE);
            return mat;
        }
        return projMat;
    }

    @ModifyVariable(
            method = "setupFrustum",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Matrix4f orthoFrustumProjMat(Matrix4f projMat) {
        if (OrthoCamera.isEnabled()) {
            return OrthoCamera.createOrthoMatrix(1.0F, 20.0F);
        }
        return projMat;
    }
}
