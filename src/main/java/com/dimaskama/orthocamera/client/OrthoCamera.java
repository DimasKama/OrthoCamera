package com.dimaskama.orthocamera.client;

import com.dimaskama.orthocamera.client.config.ModConfig;
import com.dimaskama.orthocamera.client.config.ModConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class OrthoCamera implements ClientModInitializer {

    public static final String MOD_ID = "orthocamera";
    public static final Logger LOGGER = LogManager.getLogger("OrthoCamera");
    public static final ModConfig CONFIG = new ModConfig("config/orthocamera.json", "assets/orthocamera/default_config.json");
    public static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID));
    private static final KeyMapping TOGGLE_KEY = createKeybinding("toggle", GLFW.GLFW_KEY_KP_4);
    private static final KeyMapping SCALE_INCREASE_KEY = createKeybinding("scale_increase", GLFW.GLFW_KEY_KP_SUBTRACT);
    private static final KeyMapping SCALE_DECREASE_KEY = createKeybinding("scale_decrease", GLFW.GLFW_KEY_KP_ADD);
    private static final KeyMapping OPEN_OPTIONS_KEY = createKeybinding("options", -1);
    private static final KeyMapping FIX_CAMERA_KEY = createKeybinding("fix_camera", GLFW.GLFW_KEY_KP_MULTIPLY);
    private static final KeyMapping FIXED_CAMERA_ROTATE_UP_KEY = createKeybinding("fixed_camera_rotate_up", -1);
    private static final KeyMapping FIXED_CAMERA_ROTATE_DOWN_KEY = createKeybinding("fixed_camera_rotate_down", -1);
    private static final KeyMapping FIXED_CAMERA_ROTATE_LEFT_KEY = createKeybinding("fixed_camera_rotate_left", -1);
    private static final KeyMapping FIXED_CAMERA_ROTATE_RIGHT_KEY = createKeybinding("fixed_camera_rotate_right", -1);
    private static final Component ENABLED_TEXT = Component.translatable("orthocamera.enabled");
    private static final Component DISABLED_TEXT = Component.translatable("orthocamera.disabled");
    private static final Component FIXED_TEXT = Component.translatable("orthocamera.fixed");
    private static final Component UNFIXED_TEXT = Component.translatable("orthocamera.unfixed");
    private static final float SCALE_MUL_INTERVAL = 1.1F;

    @Override
    public void onInitializeClient() {
        CONFIG.loadOrCreate();
        CONFIG.enabled &= CONFIG.save_enabled_state;
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
        KeyBindingHelper.registerKeyBinding(SCALE_INCREASE_KEY);
        KeyBindingHelper.registerKeyBinding(SCALE_DECREASE_KEY);
        KeyBindingHelper.registerKeyBinding(OPEN_OPTIONS_KEY);
        KeyBindingHelper.registerKeyBinding(FIX_CAMERA_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_UP_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_DOWN_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_LEFT_KEY);
        KeyBindingHelper.registerKeyBinding(FIXED_CAMERA_ROTATE_RIGHT_KEY);
        ClientTickEvents.START_CLIENT_TICK.register(c -> CONFIG.tick());
        ClientTickEvents.END_CLIENT_TICK.register(this::handleInput);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
    }

    private void handleInput(Minecraft client) {
        boolean messageSent = false;
        while (TOGGLE_KEY.consumeClick()) {
            CONFIG.toggle();
            client.getChatListener().handleSystemMessage(
                    CONFIG.enabled ? ENABLED_TEXT : DISABLED_TEXT,
                    true
            );
            messageSent = true;
        }
        boolean on = CONFIG.enabled;
        boolean scaleChanged = false;
        while (SCALE_INCREASE_KEY.consumeClick()) {
            if (on) {
                CONFIG.setScaleX(CONFIG.scale_x * SCALE_MUL_INTERVAL);
                CONFIG.setScaleY(CONFIG.scale_y * SCALE_MUL_INTERVAL);
                CONFIG.setDirty(true);
                scaleChanged = true;
            }
        }
        while (SCALE_DECREASE_KEY.consumeClick()) {
            if (on) {
                CONFIG.setScaleX(CONFIG.scale_x / SCALE_MUL_INTERVAL);
                CONFIG.setScaleY(CONFIG.scale_y / SCALE_MUL_INTERVAL);
                CONFIG.setDirty(true);
                scaleChanged = true;
            }
        }
        if (scaleChanged && !messageSent) {
            client.getChatListener().handleSystemMessage(
                    Component.translatable(
                            "orthocamera.scale",
                            String.format("%.1f", CONFIG.scale_x), String.format("%.1f", CONFIG.scale_y)
                    ),
                    true
            );
            messageSent = true;
        }
        boolean fixPressed = false;
        while (FIX_CAMERA_KEY.consumeClick()) {
            fixPressed = true;
            CONFIG.setFixed(!CONFIG.fixed);
        }
        if (!messageSent && fixPressed) {
            client.getChatListener().handleSystemMessage(CONFIG.fixed ? FIXED_TEXT : UNFIXED_TEXT, true);
        }
        if (FIXED_CAMERA_ROTATE_LEFT_KEY.isDown()) {
            CONFIG.setFixedYaw(CONFIG.fixed_yaw + CONFIG.fixed_rotate_speed_y);
        }
        if (FIXED_CAMERA_ROTATE_RIGHT_KEY.isDown()) {
            CONFIG.setFixedYaw(CONFIG.fixed_yaw - CONFIG.fixed_rotate_speed_y);
        }
        if (FIXED_CAMERA_ROTATE_UP_KEY.isDown()) {
            CONFIG.setFixedPitch(CONFIG.fixed_pitch + CONFIG.fixed_rotate_speed_x);
        }
        if (FIXED_CAMERA_ROTATE_DOWN_KEY.isDown()) {
            CONFIG.setFixedPitch(CONFIG.fixed_pitch - CONFIG.fixed_rotate_speed_x);
        }
        boolean openScreen = false;
        while (OPEN_OPTIONS_KEY.consumeClick()) {
            openScreen = true;
        }
        if (openScreen) {
            client.setScreen(new ModConfigScreen(null));
        }
    }

    private void onClientStopping(Minecraft client) {
        if (CONFIG.isDirty()) {
            CONFIG.save();
        }
    }

    public static boolean isEnabled() {
        return CONFIG.enabled;
    }

    public static Matrix4f createOrthoMatrix(float delta, float minScale) {
        Minecraft client = Minecraft.getInstance();
        float width = Math.max(minScale, CONFIG.getScaleX(delta)
                * client.getWindow().getWidth() / client.getWindow().getHeight());
        float height = Math.max(minScale, CONFIG.getScaleY(delta));
        return new Matrix4f().setOrtho(
                -width, width,
                -height, height,
                CONFIG.min_distance, CONFIG.max_distance
        );
    }

    private static KeyMapping createKeybinding(String name, int key) {
        return new KeyMapping(
                "orthocamera.key." + name,
                InputConstants.Type.KEYSYM,
                key,
                KEY_CATEGORY
        );
    }

}
