package org.polyfrost.evergreenhud.mixins.client;

//? if >= 26.2 {
import net.minecraft.client.renderer.extract.LevelExtractor;
//?} elif > 1.8.9 {
/*import net.minecraft.client.renderer.LevelRenderer;
*///?} else {
/*import net.minecraft.client.render.Culler;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.render.world.WorldRenderer;
*///?}
//? if > 1.8.9
import net.minecraft.client.multiplayer.ClientLevel;
import org.polyfrost.evergreenhud.client.EntityCounterEvent;
import org.polyfrost.oneconfig.api.event.v1.EventManager;
//? if >= 1.21.10
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >= 26.1 {
import net.minecraft.client.renderer.state.level.LevelRenderState;
//?} elif >= 1.21.10
//import net.minecraft.client.renderer.state.LevelRenderState;

//? if > 1.8.9 {
//~ if < 26.2 'LevelExtractor' -> 'LevelRenderer'
@Mixin(LevelExtractor.class)
//?} else
//@Mixin(WorldRenderer.class)
public abstract class Mixin_RenderGlobal_EntityCounterEvent {

    //? if > 1.8.9 {
    @Shadow private ClientLevel level;
    //?} else
    //@Shadow private int entityCount;

    //? if >= 1.21.10 {
    @Shadow @Final private LevelRenderState levelRenderState;
    //?} elif >= 1.21.2 {
    /*@Shadow private int visibleEntityCount;
    *///?} elif > 1.8.9 {
    /*@Shadow private int renderedEntities;
    *///?} else
    //@Shadow private int renderedEntityCount;

    //? if > 1.8.9 {
    @Inject(
            //? if < 1.21.2 {
            /*method = "renderLevel",
            *///?} else if < 1.21.10 {
            /*method = "renderEntities",
            *///?} else
            method = "extractVisibleEntities",
            at = @At("TAIL")
    )
    private void evergreen$readEntityRenderCount(CallbackInfo ci) {
        EntityCounterEvent.setRendered(
                //? if < 1.21.2 {
                /*this.renderedEntities
                *///?} else if < 1.21.10 {
                /*this.visibleEntityCount
                *///?} else
                this.levelRenderState.entityRenderStates.size()
        );
        EntityCounterEvent.setTotal(this.level.getEntityCount());
        EventManager.INSTANCE.post(EntityCounterEvent.INSTANCE);
    }
    //?} else {
    /*@Inject(method = "renderEntities", at = @At("TAIL"))
    private void evergreen$readEntityRenderCount(Entity entity, Culler culler, float tickDelta, CallbackInfo ci) {
        EntityCounterEvent.setRendered(this.renderedEntityCount);
        EntityCounterEvent.setTotal(this.entityCount);
        EventManager.INSTANCE.post(EntityCounterEvent.INSTANCE);
    }
    *///?}
}
