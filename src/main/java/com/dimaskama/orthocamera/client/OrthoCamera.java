package com.dimaskama.orthocamera.client;

import com.dimaskama.orthocamera.client.config.ModConfig;
import com.dimaskama.orthocamera.client.config.ModConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class OrthoCamera implements ClientModInitializer {
    public static final String MOD_ID = "orthocamera";
    public static final Logger LOGGER = LogManager.getLogger("OrthoCamera");
    public static final ModConfig CONFIG = new ModConfig("config/orthocamera.json", "assets/orthocamera/default_config.json");
    private static final KeyBinding TOGGLE_KEY = createKeybinding("toggle", GLFW.GLFW_KEY_KP_4);
    private static final KeyBinding SCALE_INCREASE_KEY = createKeybinding("scale_increase", GLFW.GLFW_KEY_KP_SUBTRACT);
    private static final KeyBinding SCALE_DECREASE_KEY = createKeybinding("scale_decrease", GLFW.GLFW_KEY_KP_ADD);
    private static final KeyBinding OPEN_OPTIONS_KEY = createKeybinding("options", -1);
    private static final Text ENABLED_TEXT = Text.translatable("orthocamera.enabled");
    private static final Text DISABLED_TEXT = Text.translatable("orthocamera.disabled");
    private static final float SCALE_MUL_INTERVAL = 1.1F;

    @Override
    public void onInitializeClient() {
        CONFIG.loadOrCreate();
        CONFIG.enabled &= CONFIG.save_enabled_state;
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);
        KeyBindingHelper.registerKeyBinding(SCALE_INCREASE_KEY);
        KeyBindingHelper.registerKeyBinding(SCALE_DECREASE_KEY);
        KeyBindingHelper.registerKeyBinding(OPEN_OPTIONS_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CONFIG.tick();
            handleInput(client);
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStopping);
    }

    private void handleInput(MinecraftClient client) {
        boolean messageSent = false;
        while (TOGGLE_KEY.wasPressed()) {
            CONFIG.enabled = !CONFIG.enabled;
            client.getMessageHandler().onGameMessage(
                    CONFIG.enabled ? ENABLED_TEXT : DISABLED_TEXT,
                    true
            );
            messageSent = true;
        }
        boolean on = CONFIG.enabled;
        boolean scaleChanged = false;
        while (SCALE_INCREASE_KEY.wasPressed()) {
            if (on) {
                CONFIG.setScaleX(CONFIG.scale_x * SCALE_MUL_INTERVAL);
                CONFIG.setScaleY(CONFIG.scale_y * SCALE_MUL_INTERVAL);
                scaleChanged = true;
            }
        }
        while (SCALE_DECREASE_KEY.wasPressed()) {
            if (on) {
                CONFIG.setScaleX(CONFIG.scale_x / SCALE_MUL_INTERVAL);
                CONFIG.setScaleY(CONFIG.scale_y / SCALE_MUL_INTERVAL);
                scaleChanged = true;
            }
        }
        if (scaleChanged && !messageSent) {
            client.getMessageHandler().onGameMessage(
                    Text.translatable(
                            "orthocamera.scale",
                            String.format("%.1f", CONFIG.scale_x), String.format("%.1f", CONFIG.scale_y)
                    ),
                    true
            );
        }
        boolean openScreen = false;
        while (OPEN_OPTIONS_KEY.wasPressed()) {
            openScreen = true;
        }
        if (openScreen) {
            client.setScreen(new ModConfigScreen(null));
        }
    }

    private void onClientStopping(MinecraftClient client) {
        if (CONFIG.isDirty()) {
            CONFIG.save();
        }
    }

    public static boolean isEnabled() {
        return CONFIG.enabled;
    }

    public static Matrix4f createOrthoMatrix(float delta, float minScale) {
        MinecraftClient client = MinecraftClient.getInstance();
        float width = Math.max(minScale, CONFIG.getScaleX(delta)
                * client.getWindow().getFramebufferWidth() / client.getWindow().getFramebufferHeight());
        float height = Math.max(minScale, CONFIG.getScaleY(delta));
        return new Matrix4f().setOrtho(
                -width, width,
                -height, height,
                CONFIG.min_distance, CONFIG.max_distance
        );
    }

    private static KeyBinding createKeybinding(String name, int key) {
        return new KeyBinding(
                "orthocamera.key." + name,
                InputUtil.Type.KEYSYM,
                key,
                MOD_ID
        );
    }
}
