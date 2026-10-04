package com.besthitboxes;

import com.besthitboxes.config.HitboxConfig;
import com.besthitboxes.gui.BestHitboxesScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class BestHitboxesClient implements ClientModInitializer {
    public static KeyMapping openMenuKey;
    public static KeyMapping toggleGlowKey;

    @Override
    public void onInitializeClient() {
        HitboxConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(BestHitboxes.MOD_ID, "main"));
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.besthitboxes.open_menu", InputConstants.Type.KEYSYM, BestHitboxes.KEY_P, category));
        toggleGlowKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.besthitboxes.toggle_glow", InputConstants.Type.KEYSYM, BestHitboxes.KEY_N, category));
        ClientTickEvents.END_CLIENT_TICK.register(BestHitboxesClient::onClientTick);
        BestHitboxes.LOGGER.info("Best Hitboxes loaded (1.21.11)");
    }

    private static void onClientTick(Minecraft client) {
        while (openMenuKey.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new BestHitboxesScreen(null));
            }
        }
        while (toggleGlowKey.consumeClick()) {
            HitboxConfig cfg = HitboxConfig.get();
            cfg.glow = !cfg.glow;
            HitboxConfig.markChanged();
            cfg.save();
            if (client.player != null) {
                client.player.displayClientMessage(
                        Component.literal("Hitbox Glow: ").withStyle(ChatFormatting.AQUA)
                                .append(Component.literal(cfg.glow ? "ON" : "OFF")
                                        .withStyle(cfg.glow ? ChatFormatting.GREEN : ChatFormatting.RED)),
                        true);
            }
        }
    }
}
