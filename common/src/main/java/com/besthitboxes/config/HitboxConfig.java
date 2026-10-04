package com.besthitboxes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.besthitboxes.BestHitboxes;
import com.besthitboxes.util.ColorUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HitboxConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_COLOR = 0xFF00E5FF;

    private static HitboxConfig instance = new HitboxConfig();

    private static int revision = 0;

    public boolean enabled = true;
    public String color = "#00E5FF";
    public boolean glow = false;
    public float glowIntensity = 0.6f;

    private transient int colorArgb = DEFAULT_COLOR;
    private transient boolean dirty = false;

    public static HitboxConfig get() {
        return instance;
    }

    public static int revision() {
        return revision;
    }

    public static void markChanged() {
        revision++;
        instance.dirty = true;
    }

    public int colorArgb() {
        return colorArgb;
    }

    public void setColorArgb(int argb) {
        colorArgb = 0xFF000000 | (argb & 0xFFFFFF);
        color = ColorUtil.toHex(colorArgb);
        markChanged();
    }

    public void setGlowIntensity(float value) {
        float clamped = Math.max(0f, Math.min(1f, value));
        glowIntensity = Math.round(clamped * 100f) / 100f;
        markChanged();
    }

    private void sanitize() {
        colorArgb = ColorUtil.parseHex(color, DEFAULT_COLOR);
        color = ColorUtil.toHex(colorArgb);
        if (Float.isNaN(glowIntensity)) glowIntensity = 0.6f;
        glowIntensity = Math.max(0f, Math.min(1f, glowIntensity));
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("besthitboxes.json");
    }

    public static void load() {
        Path file = path();
        HitboxConfig loaded = null;
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(reader, HitboxConfig.class);
            } catch (Exception e) {
                BestHitboxes.LOGGER.warn("Could not read besthitboxes.json, using defaults", e);
            }
        }
        instance = loaded != null ? loaded : new HitboxConfig();
        instance.sanitize();
        markChanged();
        instance.save();
    }

    public void save() {
        if (!dirty) return;
        dirty = false;
        Path file = path();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            BestHitboxes.LOGGER.warn("Could not save besthitboxes.json", e);
        }
    }
}
