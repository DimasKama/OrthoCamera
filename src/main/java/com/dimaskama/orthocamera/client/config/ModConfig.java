package com.dimaskama.orthocamera.client.config;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ModConfig extends JsonConfig {

    public static final float MIN_SCALE = 0.01F;
    public static final float MAX_SCALE = 10000.0F;

    private transient boolean dirty;
    private transient float prevScaleX;
    private transient float prevScaleY;
    private transient float prevFixedYaw;
    private transient float prevFixedPitch;
    private transient CameraType prevPerspective;

    public boolean enabled = false;
    public boolean save_enabled_state;
    public boolean hide_world_border;
    public float scale_x = 3.0F;
    public float scale_y = 3.0F;
    public float min_distance = -1000.0F;
    public float max_distance = 1000.0F;
    public boolean fixed = false;
    public float fixed_yaw = 0.0F;
    public float fixed_pitch = 0.0F;
    public float fixed_rotate_speed_y = 3.0F;
    public float fixed_rotate_speed_x = 3.0F;
    public boolean auto_third_person = true;

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
        prevFixedYaw = fixed_yaw;
        prevFixedPitch = fixed_pitch;
    }

    public float getScaleX(float delta) {
        return Mth.lerp(delta, prevScaleX, scale_x);
    }

    public float getScaleY(float delta) {
        return Mth.lerp(delta, prevScaleY, scale_y);
    }

    public float getFixedYaw(float delta) {
        return Mth.rotLerp(delta, prevFixedYaw, fixed_yaw);
    }

    public float getFixedPitch(float delta) {
        return Mth.rotLerp(delta, prevFixedPitch, fixed_pitch);
    }

    public void setScaleX(float scale) {
        scale = Mth.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_x) {
            scale_x = scale;
            setDirty(true);
        }
    }

    public void setScaleY(float scale) {
        scale = Mth.clamp(scale, MIN_SCALE, MAX_SCALE);
        if (scale != scale_y) {
            scale_y = scale;
            setDirty(true);
        }
    }

    public void setFixedYaw(float yaw) {
        if (yaw < 0) yaw = 360 + yaw;
        yaw = yaw % 360;
        if (yaw != fixed_yaw) {
            fixed_yaw = yaw;
            setDirty(true);
        }
    }

    public void setFixedPitch(float pitch) {
        pitch = Mth.clamp(pitch, -90.0F, 90.0F);
        if (pitch != fixed_pitch) {
            fixed_pitch = pitch;
            setDirty(true);
        }
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
        if (fixed) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity != null) {
                setFixedYaw(entity.getYRot() + 180);
                prevFixedYaw = fixed_yaw;
                setFixedPitch(entity.getXRot());
                prevFixedPitch = fixed_pitch;
            }
        }
        setDirty(true);
    }

    public void toggle() {
        enabled = !enabled;
        if (auto_third_person) {
            Minecraft client = Minecraft.getInstance();
            if (enabled) {
                prevPerspective = client.options.getCameraType();
                client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else if (prevPerspective != null) {
                client.options.setCameraType(prevPerspective);
            }
        }
        setDirty(true);
    }

}
