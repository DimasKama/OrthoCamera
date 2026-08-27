package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.duck.ProjectionDuck;
import net.minecraft.client.renderer.Projection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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

    @ModifyArgs(
            method = "getMatrix",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Matrix4f;setOrtho(FFFFFFZ)Lorg/joml/Matrix4f;"
            )
    )
    private void modifyMatrix(Args args) {
        if (orthocamera_isOrthocamera()) {
            args.set(0, -width);
            args.set(1, width);
            args.set(2, -height);
            args.set(3, height);
            args.set(4, zNear);
            args.set(5, zFar);
            // Keep argument 6 (zZeroToOne) supplied by Minecraft intact.
        }
    }

}
