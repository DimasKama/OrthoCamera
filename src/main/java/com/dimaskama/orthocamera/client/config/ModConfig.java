package com.dimaskama.orthocamera.client.config;

import net.minecraft.util.math.MathHelper;

public class ModConfig extends JsonConfig {
    public static final float MIN_SCALE = 0.01F;
    public static final float MAX_SCALE = 10000.0F;

    private transient boolean dirty;
    private transient float prevScaleX = 3.0F;
    private transient float prevScaleY = 3.0F;

    public boolean enabled;
    public boolean save_enabled_state;
    public float scale_x = 3.0F;
    public float scale_y = 3.0F;
    public float min_distance = -1000.0F;
    public float max_distance = 1000.0F;

    public ModConfig(String path, String defaultPath) {
        super(path, defaultPath);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void tick() {
        prevScaleX = scale_x;
        prevScaleY = scale_y;
    }

    public void setScaleX(float scale) {
        scale = MathHelper.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_x) {
            scale_x = scale;
            setDirty(true);
        }
    }
    public float getScaleX(float delta) {
        return MathHelper.lerp(delta, prevScaleX, scale_x);
    }

    public float getScaleY(float delta) {
        return MathHelper.lerp(delta, prevScaleY, scale_y);
    }

    public void setScaleY(float scale) {
        scale = MathHelper.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_y) {
            scale_y = scale;
            setDirty(true);
        }
    }
}
