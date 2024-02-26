package com.dimaskama.orthocamera.mixin;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.ByteBuffer;

// Higher priority than Sodium
@Mixin(value = BufferBuilder.class, priority = 1050)
public abstract class BufferBuilderMixin {
    @Shadow @Nullable private Vector3f[] sortingPrimitiveCenters;
    @Shadow private float sortingCameraX;
    @Shadow private float sortingCameraY;
    @Shadow private float sortingCameraZ;
    @Shadow private int elementOffset;
    @Shadow private ByteBuffer buffer;

    /**
     * @author JellySquid
     * @reason Copied & Modified Sodium optimization. Made for sorting indices by Z instead by the distance to camera, when Orthocamera is enabled. Needed to overwrite all of these for Sodium compatibility
     */
    @Overwrite
    private void writeSortedIndices(VertexFormat.IndexType indexType) {
        float[] distance = new float[this.sortingPrimitiveCenters.length];
        int[] indices = new int[this.sortingPrimitiveCenters.length];

        this.calculatePrimitiveDistances(distance, indices);

        IntArrays.mergeSort(indices, (a, b) -> Floats.compare(distance[b], distance[a]));

        this.writePrimitiveIndices(indexType, indices);
    }

    @Unique
    private void calculatePrimitiveDistances(float[] distance, int[] indices) {
        int i = 0;

        while (i < this.sortingPrimitiveCenters.length) {
            Vector3f pos = this.sortingPrimitiveCenters[i];

            if (pos == null) {
                throw new NullPointerException("Primitive center is null");
            }

            if (!OrthoCamera.isEnabled()) {
                float x = pos.x() - this.sortingCameraX;
                float y = pos.y() - this.sortingCameraY;
                float z = pos.z() - this.sortingCameraZ;

                distance[i] = (x * x) + (y * y) + (z * z);
            } else {
                distance[i] = -pos.z();
            }

            indices[i] = i++;
        }
    }

    @Unique
    private static final int[] VERTEX_ORDER = new int[] { 0, 1, 2, 2, 3, 0 };

    @Unique
    private void writePrimitiveIndices(VertexFormat.IndexType indexType, int[] indices) {
        long ptr = MemoryUtil.memAddress(this.buffer, this.elementOffset);

        switch (indexType) {
            case BYTE -> {
                for (int index : indices) {
                    int start = index * 4;

                    for (int offset : VERTEX_ORDER) {
                        MemoryUtil.memPutByte(ptr, (byte) (start + offset));
                        ptr += Byte.BYTES;
                    }
                }
            }
            case SHORT -> {
                for (int index : indices) {
                    int start = index * 4;

                    for (int offset : VERTEX_ORDER) {
                        MemoryUtil.memPutShort(ptr, (short) (start + offset));
                        ptr += Short.BYTES;
                    }
                }
            }
            case INT -> {
                for (int index : indices) {
                    int start = index * 4;

                    for (int offset : VERTEX_ORDER) {
                        MemoryUtil.memPutInt(ptr, (start + offset));
                        ptr += Integer.BYTES;
                    }
                }
            }
        }
    }
}
