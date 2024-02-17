package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntComparator;
import net.minecraft.client.render.BufferBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin {
    @Shadow @Nullable private Vector3f[] sortingPrimitiveCenters;

    @ModifyArg(
            method = "writeSortedIndices",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/ints/IntArrays;mergeSort([ILit/unimi/dsi/fastutil/ints/IntComparator;)V"
            ),
            index = 1
    )
    // Vertex sorting by Z
    private IntComparator afterChange(IntComparator comp) {
        if (OrthoCamera.isEnabled()) {
            float[] fs = new float[sortingPrimitiveCenters.length];
            for (int i = 0; i < fs.length; i++) {
                fs[i] = -sortingPrimitiveCenters[i].z();
            }
            return (a, b) -> Floats.compare(fs[b], fs[a]);
        }
        return comp;
    }
}
