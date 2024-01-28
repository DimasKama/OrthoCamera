package com.dimaskama.orthocamera.integration;

import com.dimaskama.orthocamera.client.config.ModConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    private final ConfigScreenFactory<ModConfigScreen> factory = ModConfigScreen::new;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return factory;
    }
}
