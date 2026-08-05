package org.polyfrost.evergreenhud.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.polyfrost.evergreenhud.client.ClientDamageEntityEvent;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//~ if = 1.8.9 'Player' -> 'PlayerEntity'
@Mixin(Player.class)
public abstract class Mixin_Player_ClientDamageEntityEvent {
    //~ if = 1.8.9 'minecraft/world/entity/Entity' -> 'minecraft/entity/Entity'
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttackTargetEntityWithCurrentItem(Entity entity, CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance().player) return;

        //~ if = 1.8.9 'Player' -> 'PlayerEntity'
        EventManager.INSTANCE.post(new ClientDamageEntityEvent((Player) (Object) this, entity));
    }
}
