package com.dimaskama.orthocamera.client.config;

import java.io.IOException;

public interface Config {

    String getPath();

    void loadOrCreate();

    void save();

    void saveWithoutCatch() throws IOException;

}
