package com.dimaskama.orthocamera.integration;

import me.jellysquid.mods.sodium.client.SodiumClientMod;

public class SodiumIntegration {

    private static boolean prevSetting;

    public static void on() {
        prevSetting = SodiumClientMod.options().performance.useBlockFaceCulling;
        SodiumClientMod.options().performance.useBlockFaceCulling = false;
    }

    public static void off() {
        SodiumClientMod.options().performance.useBlockFaceCulling = prevSetting;
    }
}
