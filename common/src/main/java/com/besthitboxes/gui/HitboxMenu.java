package com.besthitboxes.gui;

import com.besthitboxes.config.HitboxConfig;
import com.besthitboxes.util.ColorUtil;

public class HitboxMenu {
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 234;
    private static final int CARD_W = 260;

    private static final int Y_TOGGLE = 37;
    private static final int Y_COLOR = 65;
    private static final int H_COLOR = 78;
    private static final int Y_GLOW = 147;
    private static final int Y_INTENSITY = 175;
    private static final int H_ROW = 24;
    private static final int H_INTENSITY = 30;
    private static final int Y_FOOTER = 209;
    private static final int H_FOOTER = 18;

    private static final int SV_W = 72;
    private static final int SV_H = 50;
    private static final int HUE_W = 8;
    private static final int SWATCH = 13;
    private static final int SWATCH_STEP = 18;
    private static final int[] PRESETS = {0x00E5FF, 0xFF3B3B, 0x39FF14, 0xFF00E1, 0xFFC400, 0x9D4DFF, 0xFF7A00, 0xFFFFFF};

    private static final int C_DIM = 0xA0000000;
    private static final int C_PANEL = 0xF20D0F14;
    private static final int C_PANEL_BORDER = 0xFF232733;
    private static final int C_CARD = 0xFF151821;
    private static final int C_CARD_HOVER = 0xFF1B1F2B;
    private static final int C_CARD_BORDER = 0xFF262B38;
    private static final int C_CARD_BORDER_HOVER = 0xFF3A4152;
    private static final int C_TEXT = 0xFFEDF1F7;
    private static final int C_SUB = 0xFF8C95A8;
    private static final int C_OFF = 0xFF394052;
    private static final int C_DARK = 0xFF0A0C11;

    private enum Drag { NONE, SV, HUE, SLIDER }

    private final MenuHost host;
    private final HitboxConfig cfg = HitboxConfig.get();

    private float hue;
    private float sat;
    private float val;
    private float enabledAnim;
    private float glowAnim;
    private float intensityAnim;
    private long lastNanos;
    private Drag drag = Drag.NONE;
    private int listening = -1;
    private int px;
    private int py;

    public HitboxMenu(MenuHost host) {
        this.host = host;
        float[] hsv = ColorUtil.rgbToHsv(cfg.colorArgb());
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
        this.enabledAnim = cfg.enabled ? 1f : 0f;
        this.glowAnim = cfg.glow ? 1f : 0f;
        this.intensityAnim = cfg.glowIntensity;
    }

    public void layout(int width, int height) {
        px = (width - PANEL_W) / 2;
        py = Math.max(2, (height - PANEL_H) / 2);
    }

    public void render(Canvas g, int width, int height, int mouseX, int mouseY) {
        tickAnimations();
        int accent = 0xFF000000 | (cfg.colorArgb() & 0xFFFFFF);

        g.fill(0, 0, width, height, C_DIM);

        int halo = (int) (35 + 55 * glowAnim);
        rrect(g, px - 2, py - 2, PANEL_W + 4, PANEL_H + 4, ColorUtil.withAlpha(accent, halo / 2));
        rrect(g, px - 1, py - 1, PANEL_W + 2, PANEL_H + 2, ColorUtil.withAlpha(accent, halo));
        rrect(g, px, py, PANEL_W, PANEL_H, C_PANEL_BORDER);
        rrect(g, px + 1, py + 1, PANEL_W - 2, PANEL_H - 2, C_PANEL);

        renderHeader(g, accent);
        renderToggleCard(g, mouseX, mouseY, accent);
        renderColorCard(g, mouseX, mouseY, accent);
        renderGlowCard(g, mouseX, mouseY, accent);
        renderIntensityCard(g, accent);
        renderFooter(g, mouseX, mouseY, accent);
    }

    private void renderHeader(Canvas g, int accent) {
        g.gradient(px + 1, py + 1, px + PANEL_W - 1, py + 32, ColorUtil.withAlpha(accent, 40), 0x00000000);

        int tx = px + 12;
        g.textScaled("BEST HITBOXES", tx, py + 7, 1.35f, C_TEXT);
        g.text("Improve your Hitboxes", tx, py + 21, C_SUB);

        int lineY = py + 32;
        int lx0 = px + 10;
        int span = PANEL_W - 20;
        int segs = 24;
        for (int i = 0; i < segs; i++) {
            int a = lx0 + span * i / segs;
            int b = lx0 + span * (i + 1) / segs;
            float t = i / (float) (segs - 1);
            float fade = 1f - Math.abs(t - 0.5f) * 2f;
            g.fill(a, lineY, b, lineY + 1, ColorUtil.withAlpha(accent, (int) (30 + 225 * fade)));
        }

        int iw = 14;
        int ih = 20;
        int ix = px + PANEL_W - 14 - iw;
        int iy = py + 7;
        float glowStrength = glowAnim * enabledAnim * (0.35f + 0.65f * intensityAnim);
        for (int k = 3; k >= 1; k--) {
            int a = (int) (glowStrength * 120 / k);
            if (a > 2) {
                g.outline(ix - k, iy - k, iw + 2 * k, ih + 2 * k, ColorUtil.withAlpha(accent, a));
            }
        }
        g.outline(ix, iy, iw, ih, ColorUtil.lerp(0xFFFFFFFF, accent, enabledAnim));
        g.fill(ix + 1, iy + 5, ix + iw - 1, iy + 6, 0xFFFF3B3B);

        String status = cfg.enabled ? "ON" : "OFF";
        int sw = g.width(status) + 10;
        int sx = ix - 10 - sw;
        rrect(g, sx, iy + 4, sw, 12, cfg.enabled ? ColorUtil.withAlpha(accent, 60) : 0xFF2A2F3B);
        g.text(status, sx + 5, iy + 6, cfg.enabled ? accent : C_SUB);
    }

    private void renderToggleCard(Canvas g, int mx, int my, int accent) {
        int x = px + 10;
        int y = py + Y_TOGGLE;
        boolean hover = inside(mx, my, x, y, CARD_W, H_ROW);
        card(g, x, y, CARD_W, H_ROW, hover);
        badge(g, x + 7, y + 7, "01", accent);
        g.text("Mod Toggle", x + 28, y + 8, C_TEXT);
        g.text(cfg.enabled ? "Custom hitboxes" : "Vanilla hitboxes", x + 92, y + 8, C_SUB);
        drawSwitch(g, x + CARD_W - 34, y + 6, enabledAnim, accent);
    }

    private void renderColorCard(Canvas g, int mx, int my, int accent) {
        int x = px + 10;
        int y = py + Y_COLOR;
        card(g, x, y, CARD_W, H_COLOR, false);
        badge(g, x + 7, y + 6, "02", accent);
        g.text("Hitbox Colour", x + 28, y + 7, C_TEXT);

        int sx = svX();
        int sy = svY();
        g.fill(sx - 1, sy - 1, sx + SV_W + 1, sy + SV_H + 1, C_CARD_BORDER_HOVER);
        int cols = SV_W / 2;
        for (int i = 0; i < cols; i++) {
            float s = i / (float) (cols - 1);
            int top = ColorUtil.hsvToRgb(hue, s, 1f);
            int cx0 = sx + i * 2;
            g.gradient(cx0, sy, cx0 + 2, sy + SV_H, top, 0xFF000000);
        }
        int cx = sx + Math.round(sat * (SV_W - 1));
        int cy = sy + Math.round((1f - val) * (SV_H - 1));
        g.outline(cx - 3, cy - 3, 7, 7, 0xFF000000);
        g.outline(cx - 2, cy - 2, 5, 5, 0xFFFFFFFF);

        int hx = hueX();
        g.fill(hx - 1, sy - 1, hx + HUE_W + 1, sy + SV_H + 1, C_CARD_BORDER_HOVER);
        for (int k = 0; k < 6; k++) {
            int ya = sy + SV_H * k / 6;
            int yb = sy + SV_H * (k + 1) / 6;
            g.gradient(hx, ya, hx + HUE_W, yb, ColorUtil.hsvToRgb(k / 6f, 1f, 1f), ColorUtil.hsvToRgb((k + 1) / 6f, 1f, 1f));
        }
        int hc = sy + Math.round(hue * (SV_H - 1));
        g.fill(hx - 2, hc - 1, hx + HUE_W + 2, hc + 1, 0xFFFFFFFF);

        int rx = presetsX();
        rrect(g, rx - 1, sy - 1, 46, 20, C_CARD_BORDER_HOVER);
        rrect(g, rx, sy, 44, 18, accent);
        g.text(ColorUtil.toHex(accent), rx + 52, sy + 5, C_TEXT);

        for (int i = 0; i < PRESETS.length; i++) {
            int bx = rx + i * SWATCH_STEP;
            int by = presetsY();
            boolean selected = (accent & 0xFFFFFF) == PRESETS[i];
            boolean hov = inside(mx, my, bx, by, SWATCH, SWATCH);
            if (selected || hov) {
                rrect(g, bx - 1, by - 1, SWATCH + 2, SWATCH + 2, selected ? 0xFFFFFFFF : 0xFF6A7285);
            }
            rrect(g, bx, by, SWATCH, SWATCH, 0xFF000000 | PRESETS[i]);
        }
        g.text("Drag to fine-tune", rx, sy + 42, C_SUB);
    }

    private void renderGlowCard(Canvas g, int mx, int my, int accent) {
        int x = px + 10;
        int y = py + Y_GLOW;
        boolean hover = inside(mx, my, x, y, CARD_W, H_ROW);
        card(g, x, y, CARD_W, H_ROW, hover);
        badge(g, x + 7, y + 7, "03", accent);
        g.text("Hitbox Glow", x + 28, y + 8, C_TEXT);
        String keyName = host.keyName(MenuHost.KEY_GLOW);
        g.text(fit(g, "Quick key: " + keyName, 120), x + 92, y + 8, C_SUB);
        drawSwitch(g, x + CARD_W - 34, y + 6, glowAnim, accent);
    }

    private void renderIntensityCard(Canvas g, int accent) {
        int x = px + 10;
        int y = py + Y_INTENSITY;
        boolean active = cfg.glow;
        card(g, x, y, CARD_W, H_INTENSITY, false);
        badge(g, x + 7, y + 5, "04", active ? accent : C_OFF);
        g.text("Glow Intensity", x + 28, y + 6, active ? C_TEXT : C_SUB);
        String right = active ? Math.round(cfg.glowIntensity * 100f) + "%" : "Turn on Glow first";
        g.text(right, x + CARD_W - 8 - g.width(right), y + 6, active ? accent : C_SUB);

        int tx = sliderX();
        int ty = y + 20;
        int tw = sliderW();
        rrect(g, tx, ty, tw, 4, C_DARK);
        int fillW = Math.round(tw * intensityAnim);
        if (fillW > 0) {
            int from = active ? ColorUtil.withAlpha(accent, 110) : C_OFF;
            int to = active ? accent : C_OFF;
            int segs = Math.max(1, fillW / 4);
            for (int i = 0; i < segs; i++) {
                int a = tx + fillW * i / segs;
                int b = tx + fillW * (i + 1) / segs;
                g.fill(a, ty, b, ty + 4, ColorUtil.lerp(from, to, segs == 1 ? 1f : i / (float) (segs - 1)));
            }
        }
        int kx = tx + fillW - 3;
        if (active) {
            g.outline(kx - 1, ty - 4, 9, 12, ColorUtil.withAlpha(accent, 130));
        }
        rrect(g, kx, ty - 3, 7, 10, active ? 0xFFFFFFFF : 0xFF5A6070);
    }

    private void renderFooter(Canvas g, int mx, int my, int accent) {
        int y = py + Y_FOOTER;
        keyChip(g, px + 10, y, 94, "Menu", MenuHost.KEY_MENU, mx, my, accent);
        keyChip(g, px + 108, y, 94, "Glow", MenuHost.KEY_GLOW, mx, my, accent);

        int dx = px + 206;
        int dw = 64;
        boolean hov = inside(mx, my, dx, y, dw, H_FOOTER);
        rrect(g, dx, y, dw, H_FOOTER, hov ? accent : ColorUtil.withAlpha(accent, 175));
        String done = "Done";
        int textCol = ColorUtil.isLight(accent) ? 0xFF0B0D12 : 0xFFFFFFFF;
        g.text(done, dx + (dw - g.width(done)) / 2, y + 5, textCol);
    }

    private void keyChip(Canvas g, int x, int y, int w, String label, int key, int mx, int my, int accent) {
        boolean hov = inside(mx, my, x, y, w, H_FOOTER);
        boolean isListening = listening == key;
        rrect(g, x, y, w, H_FOOTER, isListening ? accent : (hov ? C_CARD_BORDER_HOVER : C_CARD_BORDER));
        rrect(g, x + 1, y + 1, w - 2, H_FOOTER - 2, (hov || isListening) ? C_CARD_HOVER : C_CARD);

        if (isListening) {
            boolean blink = (System.currentTimeMillis() / 400L) % 2L == 0L;
            String txt = "Press a key...";
            g.text(txt, x + (w - g.width(txt)) / 2, y + 5, blink ? accent : C_TEXT);
            return;
        }

        g.text(label, x + 6, y + 5, C_SUB);
        String keyName = fit(g, host.keyName(key), w - 44);
        int kw = g.width(keyName) + 8;
        int kx = x + w - 4 - kw;
        rrect(g, kx, y + 3, kw, 12, C_DARK);
        g.text(keyName, kx + 4, y + 5, accent);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!host.isLeftButton(button)) {
            return false;
        }
        if (listening >= 0) {
            listening = -1;
            return true;
        }
        int x = px + 10;
        if (inside(mx, my, x, py + Y_TOGGLE, CARD_W, H_ROW)) {
            cfg.enabled = !cfg.enabled;
            HitboxConfig.markChanged();
            return true;
        }
        if (inside(mx, my, svX(), svY(), SV_W, SV_H)) {
            drag = Drag.SV;
            updateSV(mx, my);
            return true;
        }
        if (inside(mx, my, hueX() - 2, svY(), HUE_W + 4, SV_H)) {
            drag = Drag.HUE;
            updateHue(my);
            return true;
        }
        for (int i = 0; i < PRESETS.length; i++) {
            if (inside(mx, my, presetsX() + i * SWATCH_STEP, presetsY(), SWATCH, SWATCH)) {
                applyPreset(PRESETS[i]);
                return true;
            }
        }
        if (inside(mx, my, x, py + Y_GLOW, CARD_W, H_ROW)) {
            cfg.glow = !cfg.glow;
            HitboxConfig.markChanged();
            return true;
        }
        if (cfg.glow && inside(mx, my, sliderX() - 4, py + Y_INTENSITY + 14, sliderW() + 8, 16)) {
            drag = Drag.SLIDER;
            updateSlider(mx);
            return true;
        }
        int fy = py + Y_FOOTER;
        if (inside(mx, my, px + 10, fy, 94, H_FOOTER)) {
            listening = MenuHost.KEY_MENU;
            return true;
        }
        if (inside(mx, my, px + 108, fy, 94, H_FOOTER)) {
            listening = MenuHost.KEY_GLOW;
            return true;
        }
        if (inside(mx, my, px + 206, fy, 64, H_FOOTER)) {
            host.closeMenu();
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mx, double my) {
        if (drag == Drag.SV) {
            updateSV(mx, my);
            return true;
        }
        if (drag == Drag.HUE) {
            updateHue(my);
            return true;
        }
        if (drag == Drag.SLIDER) {
            updateSlider(mx);
            return true;
        }
        return false;
    }

    public boolean mouseReleased() {
        if (drag != Drag.NONE) {
            drag = Drag.NONE;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int key) {
        if (listening < 0) {
            return false;
        }
        if (!host.isEscapeKey(key)) {
            host.rebind(listening, key);
        }
        listening = -1;
        return true;
    }

    public void onRemoved() {
        listening = -1;
        cfg.save();
    }

    private void updateSV(double mx, double my) {
        sat = clamp01((float) ((mx - svX()) / (SV_W - 1)));
        val = 1f - clamp01((float) ((my - svY()) / (SV_H - 1)));
        cfg.setColorArgb(ColorUtil.hsvToRgb(hue, sat, val));
    }

    private void updateHue(double my) {
        hue = clamp01((float) ((my - svY()) / (SV_H - 1)));
        if (hue >= 1f) hue = 0.9999f;
        cfg.setColorArgb(ColorUtil.hsvToRgb(hue, sat, val));
    }

    private void updateSlider(double mx) {
        cfg.setGlowIntensity((float) ((mx - sliderX()) / sliderW()));
    }

    private void applyPreset(int rgb) {
        cfg.setColorArgb(0xFF000000 | rgb);
        float[] hsv = ColorUtil.rgbToHsv(rgb);
        if (hsv[1] > 0.01f) {
            hue = hsv[0];
        }
        sat = hsv[1];
        val = hsv[2];
    }

    private void tickAnimations() {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0f : Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        float k = Math.min(1f, dt * 14f);
        enabledAnim += ((cfg.enabled ? 1f : 0f) - enabledAnim) * k;
        glowAnim += ((cfg.glow ? 1f : 0f) - glowAnim) * k;
        intensityAnim += (cfg.glowIntensity - intensityAnim) * Math.min(1f, dt * 20f);
    }

    private void drawSwitch(Canvas g, int x, int y, float t, int accent) {
        rrect(g, x, y, 26, 12, ColorUtil.lerp(C_OFF, accent, t));
        int kx = x + 2 + Math.round(14 * t);
        rrect(g, kx, y + 2, 8, 8, 0xFFFFFFFF);
    }

    private void card(Canvas g, int x, int y, int w, int h, boolean hover) {
        rrect(g, x, y, w, h, hover ? C_CARD_BORDER_HOVER : C_CARD_BORDER);
        rrect(g, x + 1, y + 1, w - 2, h - 2, hover ? C_CARD_HOVER : C_CARD);
    }

    private void badge(Canvas g, int x, int y, String num, int color) {
        rrect(g, x, y, 16, 11, ColorUtil.withAlpha(color, 55));
        g.text(num, x + (16 - g.width(num)) / 2, y + 2, color);
    }

    private static void rrect(Canvas g, int x, int y, int w, int h, int color) {
        if (w < 3 || h < 3) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        g.fill(x + 1, y, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private String fit(Canvas g, String s, int maxWidth) {
        if (g.width(s) <= maxWidth) return s;
        int end = s.length();
        while (end > 0 && g.width(s.substring(0, end) + "..") > maxWidth) {
            end--;
        }
        return s.substring(0, end) + "..";
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static float clamp01(float f) {
        return f < 0f ? 0f : (f > 1f ? 1f : f);
    }

    private int svX() {
        return px + 18;
    }

    private int svY() {
        return py + Y_COLOR + 20;
    }

    private int hueX() {
        return svX() + SV_W + 6;
    }

    private int presetsX() {
        return px + 112;
    }

    private int presetsY() {
        return svY() + 24;
    }

    private int sliderX() {
        return px + 18;
    }

    private int sliderW() {
        return CARD_W - 16;
    }
}
