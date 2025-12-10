package com.dimaskama.orthocamera.client.config;

import com.dimaskama.orthocamera.client.OrthoCamera;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModConfigScreen extends Screen {

    private final Screen parent;
    private final ModConfig config = OrthoCamera.CONFIG;

    public ModConfigScreen(Screen parent) {
        super(Component.translatable(OrthoCamera.MOD_ID));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int optionWidth = 180;
        int leftX = ((width - 5) >>> 1) - optionWidth;
        int rightX = (width + 5) >>> 1;
        int y = 40;
        addRenderableWidget(Button.builder(Component.translatable("orthocamera.config.enabled", textOfBool(config.enabled)), button -> {
            config.toggle();
            button.setMessage(Component.translatable("orthocamera.config.enabled", textOfBool(config.enabled)));
        }).bounds(leftX, y, optionWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("orthocamera.config.save_enabled_state", textOfBool(config.save_enabled_state)), button -> {
            config.save_enabled_state = !config.save_enabled_state;
            config.setDirty(true);
            button.setMessage(Component.translatable("orthocamera.config.save_enabled_state", textOfBool(config.save_enabled_state)));
        }).bounds(rightX, y, optionWidth, 20).build());
        y += 25;
        addRenderableWidget(new ConfigSliderWidget(
                leftX, y,
                "scale_x", config.scale_x,
                100.0F, 1.0F,
                v -> config.scale_x = v
        ));
        addRenderableWidget(new ConfigSliderWidget(
                rightX, y, "scale_y",
                config.scale_y,
                100.0F, 1.0F,
                v -> config.scale_y = v)
        );
        y += 25;
        addRenderableWidget(new ConfigSliderWidget(
                leftX, y,
                "min_distance", config.min_distance,
                1000.0F, -1000.0F,
                v -> config.min_distance = v
        ));
        addRenderableWidget(new ConfigSliderWidget(
                rightX, y,
                "max_distance", config.max_distance,
                1000.0F, 0.0F,
                v -> config.max_distance = v
        ));
        y += 25;
        addRenderableWidget(Button.builder(Component.translatable("orthocamera.config.auto_third_person", textOfBool(config.auto_third_person)), button -> {
            config.auto_third_person = !config.auto_third_person;
            config.setDirty(true);
            button.setMessage(Component.translatable("orthocamera.config.auto_third_person", textOfBool(config.auto_third_person)));
        }).bounds(leftX, y, optionWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("orthocamera.config.fixed", textOfBool(config.fixed)), button -> {
            config.setFixed(!config.fixed);
            button.setMessage(Component.translatable("orthocamera.config.fixed", textOfBool(config.fixed)));
        }).bounds(rightX, y, optionWidth, 20).build());
        y += 25;
        addRenderableWidget(new ConfigSliderWidget(
                leftX, y,
                "fixed_yaw", config.fixed_yaw,
                360.0F, 0.0F,
                v -> config.fixed_yaw = v
        ));
        addRenderableWidget(new ConfigSliderWidget(
                rightX, y,
                "fixed_pitch", config.fixed_pitch,
                180.0F, -90.0F,
                v -> config.fixed_pitch = v
        ));
        y += 25;
        addRenderableWidget(new ConfigSliderWidget(
                leftX, y,
                "fixed_rotate_speed_y", config.fixed_rotate_speed_y,
                90.0F, 0.0F,
                v -> config.fixed_rotate_speed_y = v
        ));
        addRenderableWidget(new ConfigSliderWidget(
                rightX, y,
                "fixed_rotate_speed_x", config.fixed_rotate_speed_x,
                90.0F, 0.0F,
                v -> config.fixed_rotate_speed_x = v
        ));
        y += 25;
        addRenderableWidget(Button.builder(Component.translatable("orthocamera.config.hide_world_border", textOfBool(config.hide_world_border)), button -> {
            config.hide_world_border = !config.hide_world_border;
            config.setDirty(true);
            button.setMessage(Component.translatable("orthocamera.config.hide_world_border", textOfBool(config.hide_world_border)));
        }).bounds(leftX, y, optionWidth, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("orthocamera.reset_config"), button -> {
            config.reset();
            rebuildWidgets();
        }).bounds(leftX, height - 30, optionWidth, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(rightX, height - 30, optionWidth, 20).build());
    }

    private Component textOfBool(boolean b) {
        return b ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(
                font,
                title,
                width >>> 1,
                10,
                0xFFFFFFFF
        );
    }

    @Override
    public void onClose() {
        if (config.isDirty()) {
            config.save();
            config.setDirty(false);
        }
        minecraft.setScreen(parent);
    }

    private class ConfigSliderWidget extends AbstractSliderButton {

        private final String translationKey;
        private final float multiplyFactor;
        private final float addFactor;
        private final Consumer<Float> consumer;
        private float exactValue;

        public ConfigSliderWidget(
                int x, int y,
                String name,
                float value,
                float multiplyFactor, float addFactor,
                Consumer<Float> consumer
        ) {
            super(x, y, 180, 20, CommonComponents.EMPTY, (value - addFactor) / multiplyFactor);
            this.translationKey = "orthocamera.config." + name;
            this.multiplyFactor = multiplyFactor;
            this.addFactor = addFactor;
            this.consumer = consumer;
            updateExactValue();
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(translationKey, String.format("%.1f", exactValue)));
        }

        @Override
        protected void applyValue() {
            updateExactValue();
            consumer.accept(exactValue);
            config.setDirty(true);
        }

        private void updateExactValue() {
            exactValue = (float) value * multiplyFactor + addFactor;
        }

    }

}
