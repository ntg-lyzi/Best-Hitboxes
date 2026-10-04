package com.besthitboxes.gui;

import com.besthitboxes.BestHitboxes;
import com.besthitboxes.BestHitboxesClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class BestHitboxesScreen extends Screen implements MenuHost {
    private final Screen parent;
    private final HitboxMenu menu = new HitboxMenu(this);

    public BestHitboxesScreen(Screen parent) {
        super(Component.literal("Best Hitboxes"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        menu.layout(this.width, this.height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        menu.render(new Canvas() {
            public void fill(int x1, int y1, int x2, int y2, int color) { g.fill(x1, y1, x2, y2, color); }
            public void gradient(int x1, int y1, int x2, int y2, int top, int bottom) { g.fillGradient(x1, y1, x2, y2, top, bottom); }
            public void outline(int x, int y, int w, int h, int color) { g.outline(x, y, w, h, color); }
            public void text(String s, int x, int y, int color) { g.text(font, s, x, y, color, false); }
            public void textScaled(String s, int x, int y, float scale, int color) {
                g.pose().pushMatrix();
                g.pose().translate(x, y);
                g.pose().scale(scale, scale);
                g.text(font, s, 0, 0, color, false);
                g.pose().popMatrix();
            }
            public int width(String s) { return font.width(s); }
        }, this.width, this.height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return menu.mouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return menu.mouseDragged(event.x(), event.y()) || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return menu.mouseReleased() || super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (menu.keyPressed(event.key())) {
            return true;
        }
        if (BestHitboxesClient.openMenuKey.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            ScreenCompat.setScreen(this.minecraft, parent);
        }
    }

    @Override
    public void removed() {
        menu.onRemoved();
    }

    private static KeyMapping key(int which) {
        return which == KEY_MENU ? BestHitboxesClient.openMenuKey : BestHitboxesClient.toggleGlowKey;
    }

    @Override
    public String keyName(int which) {
        return key(which).getTranslatedKeyMessage().getString();
    }

    @Override
    public void rebind(int which, int keyCode) {
        key(which).setKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode));
        KeyMapping.resetMapping();
        if (this.minecraft != null) {
            this.minecraft.options.save();
        }
    }

    @Override
    public void closeMenu() {
        onClose();
    }

    @Override
    public boolean isLeftButton(int button) {
        return button == 0;
    }

    @Override
    public boolean isEscapeKey(int keyCode) {
        return keyCode == BestHitboxes.KEY_ESCAPE;
    }
}
