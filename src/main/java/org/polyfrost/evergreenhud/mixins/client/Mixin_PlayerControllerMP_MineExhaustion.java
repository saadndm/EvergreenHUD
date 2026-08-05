// Derived from AppleSkin (https://github.com/squeek502/AppleSkin), made by squeek502.
// AppleSkin is licensed under the Unlicense (public domain).

package org.polyfrost.evergreenhud.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
//? if > 1.8.9 {
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
//?} else {
/*import net.minecraft.block.Block;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.util.math.Direction;
*///?}
import org.polyfrost.evergreenhud.client.utils.SaturationTracker;
import org.spongepowered.asm.mixin.Mixin;
//? if > 1.8.9
import org.spongepowered.asm.mixin.Unique;
//? if = 1.8.9 {
/*import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
*///?}
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//~ if = 1.8.9 'MultiPlayerGameMode' -> 'ClientPlayerInteractionManager'
@Mixin(MultiPlayerGameMode.class)
public class Mixin_PlayerControllerMP_MineExhaustion {
    //? if > 1.8.9 {
    @Unique
    private BlockState evergreenhud$stateBeingDestroyed;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void evergreenhud$captureState(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Level level = Minecraft.getInstance().level;
        evergreenhud$stateBeingDestroyed = level == null ? null : level.getBlockState(pos);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void evergreenhud$trackMineExhaustion(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = evergreenhud$stateBeingDestroyed;
        evergreenhud$stateBeingDestroyed = null;

        if (!cir.getReturnValueZ() || state == null) return;

        Player player = Minecraft.getInstance().player;
        if (player == null || !player.hasCorrectToolForDrops(state)) return;

        SaturationTracker.INSTANCE.onDestroyBlock();
    }
    //?} else {
    /*@Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "finishMiningBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/BlockState;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void evergreenhud$trackMineExhaustion(BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> cir, @Local Block block) {
        if (minecraft.player.canMineBlock(block)) {
            SaturationTracker.INSTANCE.onDestroyBlock();
        }
    }
    *///?}
}
