package org.polyfrost.evergreenhud.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.objectweb.asm.Opcodes;
import org.polyfrost.evergreenhud.client.SelectedItemChangedEvent;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
import org.spongepowered.asm.mixin.Mixin;
//? if = 1.8.9 {
/*import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
*///?}
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//~ if = 1.8.9 'Inventory' -> 'PlayerInventory'
@Mixin(Inventory.class)
public abstract class Mixin_InventoryPlayer_SelectedItemChangedEvent {
    //? if > 1.8.9 {
    @Inject(
            method = {
                    //? if >= 1.21.5 {
                    "setSelectedSlot",
                    //?} elif >= 1.21.4 {
                    /*"setSelectedHotbarSlot",
                    *///?} else
                    //"swapPaint",
                    "pickSlot", "replaceWith"
            },
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/world/entity/player/Inventory;selected:I",
                    shift = At.Shift.AFTER
            )
    )
    private void selectedItemChangeCallback(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            EventManager.INSTANCE.post(new SelectedItemChangedEvent(player.getMainHandItem()));
        }
    }
    //?} else {
    /*@Shadow
    public PlayerEntity player;

    @Inject(
            method = "selectSlot",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/player/PlayerInventory;selectedSlot:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void evergreenhud$selectedByItem(CallbackInfo ci) {
        evergreenhud$postSelectedItem();
    }

    @Inject(method = "scrollInHotbar", at = @At("RETURN"))
    private void evergreenhud$selectedByScroll(CallbackInfo ci) {
        evergreenhud$postSelectedItem();
    }

    @Inject(
            method = "copy",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/living/player/PlayerInventory;selectedSlot:I",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void evergreenhud$selectedByCopy(CallbackInfo ci) {
        evergreenhud$postSelectedItem();
    }

    @Unique
    private void evergreenhud$postSelectedItem() {
        if (this.player == Minecraft.getInstance().player) {
            EventManager.INSTANCE.post(new SelectedItemChangedEvent(this.player.inventory.getSelectedItem()));
        }
    }
    *///?}
}
