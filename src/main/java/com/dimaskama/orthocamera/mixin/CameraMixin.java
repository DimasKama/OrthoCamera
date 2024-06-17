package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyVariable(method = "moveBy", at = @At("HEAD"), index = 1, argsOnly = true)
    private float moveByHeadX(float value) {
        return OrthoCamera.isEnabled() ? 0.0F : value;
    }

    @ModifyVariable(method = "moveBy", at = @At("HEAD"), index = 3, argsOnly = true)
    private float moveByHeadZ(float value) {
        return OrthoCamera.isEnabled() ? 0.0F : value;
    }
}
