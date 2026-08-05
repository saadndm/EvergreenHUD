package org.polyfrost.evergreenhud.mixins.client;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.polyfrost.evergreenhud.client.hooks.PlayerPreviewPartialTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
//? if = 1.8.9 {
/*import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
*///?}
import org.spongepowered.asm.mixin.injection.ModifyConstant;

//~ if = 1.8.9 'InventoryScreen' -> 'SurvivalInventoryScreen'
@Mixin(InventoryScreen.class)
public class Mixin_InventoryScreen_PlayerPreviewPartialTick {

    //? if > 1.8.9 {
    @ModifyConstant(
            //? if <1.21.4 {
            /*method = "method_29977",
            *///?} elif <1.21.8 {
            /*method = "method_64045",
            *///?} elif <1.21.11 {
            /*method = "renderEntityInInventory",
            *///?} else {
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            //?}
            constant = @Constant(floatValue = 1.0F)
    )
    //?} else {
    /*@ModifyArg(
            method = "renderEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFF)Z"
            ),
            index = 5
    )
    *///?}
    private static float evergreenhud$playerPreviewPartialTick(float original) {
        float override = PlayerPreviewPartialTick.getPlayerPreviewPartialTick();
        return override < 0.0F ? original : override;
    }

}
