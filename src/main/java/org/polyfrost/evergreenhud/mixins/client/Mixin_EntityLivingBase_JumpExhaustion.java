// Derived from AppleSkin (https://github.com/squeek502/AppleSkin), made by squeek502.
// AppleSkin is licensed under the Unlicense (public domain).

package org.polyfrost.evergreenhud.mixins.client;

import net.minecraft.world.entity.LivingEntity;
import org.polyfrost.evergreenhud.client.utils.SaturationTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class Mixin_EntityLivingBase_JumpExhaustion {
    //~ if = 1.8.9 'jumpFromGround' -> 'jump'
    @Inject(method = "jumpFromGround", at = @At("RETURN"))
    private void evergreenhud$trackJumpExhaustion(CallbackInfo ci) {
        SaturationTracker.INSTANCE.onJump((LivingEntity) (Object) this);
    }
}
