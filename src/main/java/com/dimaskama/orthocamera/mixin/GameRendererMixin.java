package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.ProjectionType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
            ),
            index = 0
    )
    private Matrix4f modifyProjMat(Matrix4f original, @Local(argsOnly = true) RenderTickCounter tickCounter, @Local(ordinal = 0) LocalRef<Matrix4f> localMat) {
        if (OrthoCamera.isEnabled()) {
            Matrix4f mat = OrthoCamera.createOrthoMatrix(tickCounter.getTickProgress(false), 0.0F);
            localMat.set(mat);
            return mat;
        }
        return original;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/systems/ProjectionType;)V"
            ),
            index = 1
    )
    private ProjectionType modifyProjType(ProjectionType original) {
        if (OrthoCamera.isEnabled()) {
            return ProjectionType.ORTHOGRAPHIC;
        }
        return original;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"

            ),
            index = 6
    )
    private Matrix4f frustumOorthoProjMat(Matrix4f original) {
        if (OrthoCamera.isEnabled()) {
            return OrthoCamera.createOrthoMatrix(1.0F, 20.0F);
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;conjugate(Lorg/joml/Quaternionf;)Lorg/joml/Quaternionf;",
                    remap = false
            )
    )
    private Quaternionf modifyRotation(Quaternionf original, @Local(argsOnly = true) RenderTickCounter tickCounter) {
        if (OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed) {
            float delta = tickCounter.getTickProgress(false);
            return original.rotationXYZ(
                    OrthoCamera.CONFIG.getFixedPitch(delta) * MathHelper.RADIANS_PER_DEGREE,
                    OrthoCamera.CONFIG.getFixedYaw(delta) * MathHelper.RADIANS_PER_DEGREE - MathHelper.PI,
                    0.0F
            );
        }
        return original;
    }

}
