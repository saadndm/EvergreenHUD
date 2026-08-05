// Derived from AppleSkin (https://github.com/squeek502/AppleSkin), made by squeek502.
// AppleSkin is licensed under the Unlicense (public domain).

package org.polyfrost.evergreenhud.mixins.client;

import net.minecraft.world.entity.player.Player;
import org.polyfrost.evergreenhud.client.utils.SaturationTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//~ if = 1.8.9 'Player' -> 'PlayerEntity'
@Mixin(Player.class)
public class Mixin_Player_FoodExhaustion {
    //~ if = 1.8.9 'causeFoodExhaustion' -> 'addFatigue'
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"))
    private void evergreenhud$trackExhaustion(float amount, CallbackInfo ci) {
        //~ if = 1.8.9 'Player' -> 'PlayerEntity'
        SaturationTracker.INSTANCE.onCauseFoodExhaustion((Player) (Object) this, amount);
    }
}
