package com.besthitboxes.render;

import com.besthitboxes.config.HitboxConfig;
import com.besthitboxes.util.ColorUtil;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class HitboxRenderer {
    private static final int MAX_GLOW_LAYERS = 4;
    private static final GizmoStyle[] NO_GLOW = new GizmoStyle[0];

    private static int builtRevision = Integer.MIN_VALUE;
    private static GizmoStyle mainStyle;
    private static GizmoStyle serverStyle;
    private static GizmoStyle[] glowStyles = NO_GLOW;

    private HitboxRenderer() {
    }

    public static void render(Entity entity, float partialTick, boolean serverSide) {
        rebuildIfNeeded();

        Vec3 offset = entity.getPosition(partialTick).subtract(entity.position());
        AABB box = entity.getBoundingBox().move(offset);
        drawBox(box, serverSide);

        if (entity instanceof EnderDragon dragon) {
            for (Entity part : dragon.getSubEntities()) {
                drawBox(part.getBoundingBox().move(offset), serverSide);
            }
        }
    }

    private static void drawBox(AABB box, boolean serverSide) {
        if (serverSide) {
            Gizmos.cuboid(box, serverStyle);
            return;
        }
        GizmoStyle[] glow = glowStyles;
        for (int i = glow.length - 1; i >= 0; i--) {
            Gizmos.cuboid(box, glow[i]);
        }
        Gizmos.cuboid(box, mainStyle);
    }

    private static void rebuildIfNeeded() {
        int rev = HitboxConfig.revision();
        if (rev == builtRevision) return;
        builtRevision = rev;

        HitboxConfig cfg = HitboxConfig.get();
        int rgb = cfg.colorArgb() & 0xFFFFFF;
        mainStyle = GizmoStyle.stroke(0xFF000000 | rgb);
        serverStyle = GizmoStyle.stroke(0x99000000 | rgb);

        if (!cfg.glow || cfg.glowIntensity <= 0.001f) {
            glowStyles = NO_GLOW;
            return;
        }

        float t = cfg.glowIntensity;
        int layers = Math.max(1, Math.round(t * MAX_GLOW_LAYERS));
        float spread = 2.0f + 3.5f * t;
        float baseAlpha = 0.22f + 0.48f * t;
        int bright = ColorUtil.lighten(rgb, 0.30f);

        GizmoStyle[] styles = new GizmoStyle[layers];
        for (int i = 0; i < layers; i++) {
            float outward = (i + 1) / (float) layers;
            int alpha = Math.round(255f * baseAlpha * (1.0f - outward * 0.75f));
            float width = 3.0f + spread * (i + 1);
            int layerRgb = i == 0 ? bright : rgb;
            styles[i] = GizmoStyle.stroke(ColorUtil.withAlpha(layerRgb, alpha), width);
        }
        glowStyles = styles;
    }
}
