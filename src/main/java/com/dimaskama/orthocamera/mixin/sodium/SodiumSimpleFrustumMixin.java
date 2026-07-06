package com.dimaskama.orthocamera.mixin.sodium;

import com.dimaskama.orthocamera.client.OrthoCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = "net.caffeinemc.mods.sodium.client.render.viewport.frustum.SimpleFrustum",
    remap = false
)
public class SodiumSimpleFrustumMixin {

    @Inject(
        method = "testSection",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private void bypassTestSection(CallbackInfoReturnable<Boolean> cir) {
        if (OrthoCamera.isEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "testSectionExpanded",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private void bypassTestSectionExpanded(CallbackInfoReturnable<Boolean> cir) {
        if (OrthoCamera.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
