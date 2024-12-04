package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
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
                    target = "Lnet/minecraft/client/render/WorldRenderer;setupFrustum(Lnet/minecraft/util/math/Vec3d;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
            ),
            index = 2
    )
    private Matrix4f orthoFrustumProjMat(Matrix4f projMat) {
        if (OrthoCamera.isEnabled()) {
            return OrthoCamera.createOrthoMatrix(1.0F, 20.0F);
        }
        return projMat;
    }

    @ModifyArg(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"

            ),
            index = 6
    )
    private Matrix4f orthoProjMat(Matrix4f projMat, @Local(argsOnly = true) RenderTickCounter tickCounter) {
        if (OrthoCamera.isEnabled()) {
            Matrix4f mat = OrthoCamera.createOrthoMatrix(tickCounter.getTickDelta(false), 0.0F);
            RenderSystem.setProjectionMatrix(mat, ProjectionType.ORTHOGRAPHIC);
            return mat;
        }
        return projMat;
    }

    @ModifyExpressionValue(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;conjugate(Lorg/joml/Quaternionf;)Lorg/joml/Quaternionf;"
            )
    )
    private Quaternionf modifyRotation(Quaternionf original, @Local(argsOnly = true) RenderTickCounter tickCounter) {
        if (OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed) {
            float delta = tickCounter.getTickDelta(false);
            return original.rotationXYZ(
                    OrthoCamera.CONFIG.getFixedPitch(delta) * MathHelper.RADIANS_PER_DEGREE,
                    OrthoCamera.CONFIG.getFixedYaw(delta) * MathHelper.RADIANS_PER_DEGREE - MathHelper.PI,
                    0.0F
            );
        }
        return original;
    }
}
