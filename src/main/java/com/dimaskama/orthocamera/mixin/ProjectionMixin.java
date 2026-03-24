package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.duck.ProjectionDuck;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.Projection;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projection.class)
abstract class ProjectionMixin implements ProjectionDuck {

    @Shadow
    private float width;
    @Shadow
    private float height;
    @Shadow
    private float zNear;
    @Shadow
    private float zFar;

    @Unique
    private boolean orthocamera_isOrthocamera;

    @Override
    public void orthocamera_setIsOrthocamera(boolean isOrthocamera) {
        orthocamera_isOrthocamera = isOrthocamera;
    }

    @Override
    public boolean orthocamera_isOrthocamera() {
        return orthocamera_isOrthocamera;
    }

    @Inject(
            method = {"setupOrtho", "setupPerspective"},
            at = @At("TAIL")
    )
    private void afterSetup(CallbackInfo ci) {
        orthocamera_setIsOrthocamera(false);
    }

    @ModifyExpressionValue(
            method = "getMatrix",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Matrix4f;setOrtho(FFFFFFZ)Lorg/joml/Matrix4f;"
            )
    )
    private Matrix4f modifyMatrix(Matrix4f original) {
        return orthocamera_isOrthocamera() ? original.setOrtho(
                -width, width,
                -height, height,
                zNear, zFar
        ) : original;
    }

}
