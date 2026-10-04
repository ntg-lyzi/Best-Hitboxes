package com.besthitboxes;

import com.besthitboxes.config.HitboxConfig;
import com.besthitboxes.gui.BestHitboxesScreen;
import com.besthitboxes.gui.ScreenCompat;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class BestHitboxesClient implements ClientModInitializer {
    public static KeyMapping openMenuKey;
    public static KeyMapping toggleGlowKey;

    @Override
    public void onInitializeClient() {
        HitboxConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(BestHitboxes.MOD_ID, "main"));
        openMenuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.besthitboxes.open_menu", InputConstants.Type.KEYBOARD, InputConstants.KEY_P, category));
        toggleGlowKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.besthitboxes.toggle_glow", InputConstants.Type.KEYBOARD, InputConstants.KEY_N, category));
        ClientTickEvents.END_CLIENT_TICK.register(BestHitboxesClient::onClientTick);
        BestHitboxes.LOGGER.info("Best Hitboxes loaded (26.3)");
    }

    private static void onClientTick(Minecraft client) {
        while (openMenuKey.consumeClick()) {
            ScreenCompat.openMenu(client, new BestHitboxesScreen(null));
        }
        while (toggleGlowKey.consumeClick()) {
            HitboxConfig cfg = HitboxConfig.get();
            cfg.glow = !cfg.glow;
            HitboxConfig.markChanged();
            cfg.save();
        }
    }
}
