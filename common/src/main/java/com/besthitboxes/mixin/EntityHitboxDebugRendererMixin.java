package com.besthitboxes.mixin;

import com.besthitboxes.config.HitboxConfig;
import com.besthitboxes.render.HitboxRenderer;
import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {
    @Inject(method = "showHitboxes", at = @At("HEAD"), cancellable = true)
    private void besthitboxes$showHitboxes(Entity entity, float partialTick, boolean inLocalServer, CallbackInfo ci) {
        if (!HitboxConfig.get().enabled) {
            return;
        }
        HitboxRenderer.render(entity, partialTick, inLocalServer);
        ci.cancel();
    }
}
