package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.dimaskama.orthocamera.duck.FrustumDuck;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    @Final
    private Projection projection;

    @Shadow
    private Frustum cullFrustum;

    @ModifyVariable(method = "move", at = @At("HEAD"), index = 1, argsOnly = true)
    private float moveByHeadX(float value) {
        return OrthoCamera.isEnabled() ? 0.0F : value;
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), index = 3, argsOnly = true)
    private float moveByHeadZ(float value) {
        return OrthoCamera.isEnabled() ? 0.0F : value;
    }

    @Inject(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setupPerspective(FFFFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterSetupPerspective(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (OrthoCamera.isEnabled()) {
            OrthoCamera.setupOrthoMatrix(projection, deltaTracker.getGameTimeDeltaPartialTick(false));
        }
    }

    @ModifyArgs(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setRotation(FF)V"
            )
    )
    private void modifyRotation(Args args, @Local(argsOnly = true) float partialTick) {
        if (OrthoCamera.isEnabled() && OrthoCamera.CONFIG.fixed) {
            args.set(0, OrthoCamera.CONFIG.getFixedYaw(partialTick));
            args.set(1, OrthoCamera.CONFIG.getFixedPitch(partialTick));
        }
    }

    @Inject(method = "prepareCullFrustum", at = @At("TAIL"))
    private void prepareCullFrustumTail(CallbackInfo ci) {
        if (OrthoCamera.isEnabled()) {
            ((FrustumDuck) cullFrustum).orthocamera_setIsOrthocamera(true);
        }
    }

}
