package org.polyfrost.evergreenhud.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.objectweb.asm.Opcodes;
import org.polyfrost.evergreenhud.client.SelectedItemChangedEvent;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class Mixin_Minecraft_SelectedItemChangedEvent {
    //? if > 1.8.9 {
    @Inject(
            method = "handleKeybinds",
            at = @At(
                    //? if <= 1.21.4 {
                    /*value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/player/Inventory;selected:I",
                    *///?} else {
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V",
                    //?}
                    shift = At.Shift.AFTER
            ),
            require = 0
    )
    //?} else {
    /*@Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/player/PlayerInventory;selectedSlot:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    *///?}
    private void selectedItemChangeCallback(CallbackInfo ci) {
        //~ if = 1.8.9 'LocalPlayer' -> 'LocalClientPlayerEntity'
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            //~ if = 1.8.9 'player.getMainHandItem()' -> 'player.inventory.getSelectedItem()'
            EventManager.INSTANCE.post(new SelectedItemChangedEvent(player.getMainHandItem()));
        }
    }
}
