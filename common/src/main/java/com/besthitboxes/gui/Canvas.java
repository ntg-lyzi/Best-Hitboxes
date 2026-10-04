package com.besthitboxes.gui;

public interface Canvas {
    void fill(int x1, int y1, int x2, int y2, int color);
    void gradient(int x1, int y1, int x2, int y2, int topColor, int bottomColor);
    void outline(int x, int y, int width, int height, int color);
    void text(String text, int x, int y, int color);
    void textScaled(String text, int x, int y, float scale, int color);
    int width(String text);
}
