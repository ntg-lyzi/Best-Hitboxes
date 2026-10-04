package com.besthitboxes.util;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int hsvToRgb(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        float r, g, b;
        float hh = h * 6f;
        int i = Math.min(5, (int) hh);
        float f = hh - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);
        if (i == 0) { r = v; g = t; b = p; }
        else if (i == 1) { r = q; g = v; b = p; }
        else if (i == 2) { r = p; g = v; b = t; }
        else if (i == 3) { r = p; g = q; b = v; }
        else if (i == 4) { r = t; g = p; b = v; }
        else { r = v; g = p; b = q; }
        return 0xFF000000 | (to255(r) << 16) | (to255(g) << 8) | to255(b);
    }

    public static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h = 0f;
        if (d > 1.0e-6f) {
            if (max == r) h = ((g - b) / d) % 6f;
            else if (max == g) h = (b - r) / d + 2f;
            else h = (r - g) / d + 4f;
            h /= 6f;
            if (h < 0f) h += 1f;
        }
        float s = max <= 0f ? 0f : d / max;
        return new float[]{h, s, max};
    }

    public static int withAlpha(int color, int alpha) {
        int a = Math.max(0, Math.min(255, alpha));
        return (a << 24) | (color & 0xFFFFFF);
    }

    public static int lerp(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * t);
        int r = (int) (((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * t);
        int g = (int) (((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lighten(int rgb, float amount) {
        return lerp(0xFF000000 | rgb, 0xFFFFFFFF, amount) & 0xFFFFFF;
    }

    public static boolean isLight(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        return (0.299 * r + 0.587 * g + 0.114 * b) > 150;
    }

    public static String toHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    public static int parseHex(String s, int fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        if (t.startsWith("#")) t = t.substring(1);
        if (t.length() != 6) return fallback;
        try {
            return 0xFF000000 | Integer.parseInt(t, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int to255(float f) {
        return Math.max(0, Math.min(255, Math.round(f * 255f)));
    }
}
