package com.besthitboxes.gui;

public interface MenuHost {
    int KEY_MENU = 0;
    int KEY_GLOW = 1;

    String keyName(int which);
    void rebind(int which, int keyCode);
    void closeMenu();
    boolean isLeftButton(int button);
    boolean isEscapeKey(int keyCode);
}
