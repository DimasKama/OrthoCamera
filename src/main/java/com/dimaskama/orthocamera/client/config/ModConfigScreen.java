package com.dimaskama.orthocamera.client.config;

import com.dimaskama.orthocamera.client.OrthoCamera;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ModConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config = OrthoCamera.CONFIG;

    public ModConfigScreen(Screen parent) {
        super(Text.translatable(OrthoCamera.MOD_ID));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int optionWidth = 150;
        int leftX = ((width - 20) >>> 1) - optionWidth;
        int rightX = (width + 20) >>> 1;
        int y = 120;
        addDrawableChild(ButtonWidget.builder(Text.translatable("orthocamera.config.enabled", textOfBool(config.enabled)), button -> {
            config.enabled = !config.enabled;
            config.setDirty(true);
            button.setMessage(Text.translatable("orthocamera.config.enabled", textOfBool(config.enabled)));
        }).position(leftX, y).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("orthocamera.config.save_enabled_state", textOfBool(config.save_enabled_state)), button -> {
            config.save_enabled_state = !config.save_enabled_state;
            config.setDirty(true);
            button.setMessage(Text.translatable("orthocamera.config.save_enabled_state", textOfBool(config.save_enabled_state)));
        }).position(rightX, y).build());
        y += 30;
        addDrawableChild(new ConfigSliderWidget(
                leftX, y,
                "scale_x", config.scale_x,
                100.0F, 1.0F,
                v -> config.scale_x = v
        ));
        addDrawableChild(new ConfigSliderWidget(
                rightX, y, "scale_y",
                config.scale_y,
                100.0F, 1.0F,
                v -> config.scale_y = v)
        );
        y += 30;
        addDrawableChild(new ConfigSliderWidget(
                leftX, y,
                "min_distance", config.min_distance,
                1000.0F, -1000.0F,
                v -> config.min_distance = v
        ));
        addDrawableChild(new ConfigSliderWidget(
                rightX, y,
                "max_distance", config.max_distance,
                1000.0F, 0.0F,
                v -> config.max_distance = v
        ));

        addDrawableChild(ButtonWidget.builder(Text.translatable("orthocamera.reset_config"), button -> {
            config.reset();
            clearAndInit();
        }).position(leftX, height - 40).build());
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                .position(rightX, height - 40).build());
    }

    private Text textOfBool(boolean b) {
        return b ? ScreenTexts.ON : ScreenTexts.OFF;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(
                textRenderer,
                title,
                width >>> 1,
                10,
                0xFFFFFFFF
        );
    }

    @Override
    public void close() {
        if (config.isDirty()) {
            config.save();
            config.setDirty(false);
        }
        client.setScreen(parent);
    }

    @Override
    public void renderBackground(DrawContext context) {
        if (parent == null && client.world != null) {
            context.fill(0, 0, width, height, 0x50101010);
        } else {
            renderBackgroundTexture(context);
        }
    }

    private class ConfigSliderWidget extends SliderWidget {
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
            super(x, y, 150, 20, ScreenTexts.EMPTY, (value - addFactor) / multiplyFactor);
            this.translationKey = "orthocamera.config." + name;
            this.multiplyFactor = multiplyFactor;
            this.addFactor = addFactor;
            this.consumer = consumer;
            updateExactValue();
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.translatable(translationKey, String.format("%.1f", exactValue)));
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
