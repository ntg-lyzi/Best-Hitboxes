package com.besthitboxes.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenCompat {
    private ScreenCompat() {
    }

    public static void openMenu(Minecraft client, Screen menu) {
        client.gui.setScreen(menu);
    }

    public static void setScreen(Minecraft client, Screen screen) {
        client.gui.setScreen(screen);
    }
}
