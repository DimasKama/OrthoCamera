package com.dimaskama.orthocamera.client.config;

import com.dimaskama.orthocamera.client.OrthoCamera;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

public abstract class JsonConfig implements Config {

    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final transient String path;
    private final transient String defaultPath;

    public JsonConfig(String path, String defaultPath) {
        this.path = path;
        this.defaultPath = defaultPath;
    }

    @Override
    public String getPath() {
        return path;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    @Override
    public void loadOrCreate() {
        File file = new File(getPath());
        if (!file.exists()) {
            if (tryLoadDefault()) {
                return;
            }
            File parent = file.getParentFile();
            if (!(parent.exists() || parent.mkdirs())) {
                OrthoCamera.LOGGER.warn("Can't create config: " + file.getAbsolutePath());
                return;
            }
            try {
                saveWithoutCatch();
            } catch (IOException e) {
                OrthoCamera.LOGGER.warn("Exception occurred while writing new config. ", e);
            }
        } else {
            load(file);
        }
    }

    protected boolean tryLoadDefault() {
        File defaultFile = new File(defaultPath);
        if (defaultFile.exists()) {
            load(defaultFile);
            return true;
        }
        return false;
    }

    private void load(File file) {
        try (FileReader f = new FileReader(file)) {
            deserialize(JsonParser.parseReader(f));
        } catch (Exception e) {
            OrthoCamera.LOGGER.warn("Exception occurred while reading config. ", e);
        }
    }

    protected void deserialize(JsonElement element) {
        JsonConfig c = GSON.fromJson(element, getClass());
        for (Field field : getClass().getDeclaredFields()) {
            try {
                field.set(this, field.get(c));
            } catch (IllegalAccessException ignored) {}
        }
    }

    @Override
    public void save() {
        save(true);
    }

    public void save(boolean log) {
        try {
            saveWithoutCatch();
            if (log) OrthoCamera.LOGGER.info("Config saved: " + getPath());
        } catch (IOException e) {
            OrthoCamera.LOGGER.warn("Exception occurred while saving config. ", e);
        }
    }

    @Override
    public void saveWithoutCatch() throws IOException {
        try (FileWriter w = new FileWriter(getPath())) {
            GSON.toJson(serialize(), w);
        }
    }

    protected JsonElement serialize() {
        return GSON.toJsonTree(this);
    }

    public void reset() {
        try {
            JsonConfig m = getClass().getConstructor(String.class, String.class).newInstance(getPath(), getDefaultPath());
            m.tryLoadDefault();
            for (Field field : getClass().getDeclaredFields()) {
                try {
                    field.set(this, field.get(m));
                } catch (IllegalAccessException ignored) {}
            }
        } catch (Exception e) {
            OrthoCamera.LOGGER.error("Can't call config constructor. ", e);
        }
    }

}
