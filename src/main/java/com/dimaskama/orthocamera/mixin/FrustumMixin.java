package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.duck.FrustumDuck;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
abstract class FrustumMixin implements FrustumDuck {

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

    @Inject(method = "set", at = @At("TAIL"))
    private void setTail(Frustum frustum, CallbackInfo ci) {
        orthocamera_isOrthocamera = ((FrustumDuck) frustum).orthocamera_isOrthocamera();
    }

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void modifyIsVisible(CallbackInfoReturnable<Boolean> cir) {
        if (orthocamera_isOrthocamera) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "cubeInFrustum(DDDDDD)I", at = @At("HEAD"), cancellable = true)
    private void modifyCubeInFrustum(CallbackInfoReturnable<Integer> cir) {
        if (orthocamera_isOrthocamera) {
            cir.setReturnValue(FrustumIntersection.INSIDE);
        }
    }

    @Inject(method = "pointInFrustum", at = @At("HEAD"), cancellable = true)
    private void modifyPointInFrustum(CallbackInfoReturnable<Boolean> cir) {
        if (orthocamera_isOrthocamera) {
            cir.setReturnValue(true);
        }
    }

}
