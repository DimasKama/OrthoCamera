package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.render.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
public abstract class FrustumMixin {
    @Inject(
            method = "method_38557",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelMoving(int i, CallbackInfoReturnable<Frustum> cir) {
        if (OrthoCamera.isEnabled()) {
            cir.setReturnValue((Frustum) (Object) this);
        }
    }
}
