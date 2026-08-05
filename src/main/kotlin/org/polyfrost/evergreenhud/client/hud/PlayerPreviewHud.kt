package org.polyfrost.evergreenhud.client.hud

//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.GuiGraphics
*///?} else {
/*import net.minecraft.client.gui.GuiElement
import net.minecraft.client.render.platform.GlStateManager
*///?}
import net.minecraft.client.gui.screens.inventory.InventoryScreen
//? if > 1.8.9 {
import net.minecraft.util.Mth
//?} else
//import net.minecraft.util.math.MathHelper
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.evergreenhud.client.hooks.playerPreviewPartialTick
import org.polyfrost.evergreenhud.client.hooks.smuggledHudPartialTick
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc

class PlayerPreviewHud : LegacyHud(
    id = "player_preview.json",
    title = "Player Preview",
    category = Category.PLAYER,
), HudBackground {
    @Switch(title = "Paper Doll", description = "Mirror the player's own head rotation.")
    var paperDoll = false

    @Slider(title = "Rotation", min = 0F, max = 360F, step = 1F)
    var rotation = 180f

    @Slider(title = "Pitch", min = -90F, max = 90F, step = 1F)
    var pitch = 0f

    @Switch(title = "Background")
    override var background = true

    @Color(title = "Background Color")
    override var backgroundColor = PolyColor(0x90000000.toInt())

    override val width get() = 80f
    override val height get() = 120f

    override fun defaultPosition(): Pair<Float, Float> = 0f to 0f

    override fun setup() {
        super.setup()
        staticWidth = true
        if (isReal) {
            hideIf("rotation") { paperDoll }
            hideIf("pitch") { paperDoll }
            hideIf("backgroundColor") { !background }
        }
    }

    override fun update() = false

    //? if > 1.8.9 {
    override fun render(graphics: GuiGraphics) {
    //?} else
    //override fun render() {
        backgroundArgb?.let {
            //~ if = 1.8.9 'graphics' -> 'GuiElement'
            graphics.fill(0, 0, width.toInt(), height.toInt(), it)
        }

        val player = mc.player ?: return

        val scale = effectiveScale
        val x1 = x.toInt()
        val y1 = y.toInt()
        val x2 = (x + width * scale).toInt()
        val y2 = (y + height * scale).toInt()
        val entityScale = (40f * scale).toInt()

        val centerX = (x1 + x2) / 2f
        val centerY = (y1 + y2) / 2f

        val partialTick = smuggledHudPartialTick

        val yawOffset: Float
        val pitchOffset: Float
        if (paperDoll) {
            //? if > 1.8.9 {
            val bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot)
            yawOffset = Mth.wrapDegrees(player.yRot - bodyYaw)
            pitchOffset = player.xRot
            //?} else {
            /*val bodyYaw = player.lastBodyYaw + partialTick * MathHelper.wrapDegrees(player.bodyYaw - player.lastBodyYaw)
            yawOffset = MathHelper.wrapDegrees(player.yaw - bodyYaw)
            pitchOffset = player.pitch
            *///?}
        } else {
            yawOffset = rotation - 180f
            pitchOffset = pitch
        }

        //? if = 1.8.9 {
        /*val lastBodyYaw = player.lastBodyYaw
        val lastYaw = player.lastYaw
        val lastPitch = player.lastPitch
        player.lastBodyYaw = (Math.atan((yawOffset / 40f).toDouble()) * 20f).toFloat()
        player.lastYaw = (Math.atan((yawOffset / 40f).toDouble()) * 40f).toFloat()
        player.lastPitch = (Math.atan((pitchOffset / 40f).toDouble()) * 20f).toFloat()
        *///?}
        playerPreviewPartialTick = partialTick
        try {
            //? if > 1.8.9 {
            //? if < 1.21.8 {
            /*graphics.pose().pushPose()
            graphics.pose().last().pose().identity()
            *///?}
            //? if < 26
            //InventoryScreen.renderEntityInInventoryFollowsMouse(
            //? if >= 26
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                graphics,
                x1, y1, x2, y2,
                entityScale,
                0.0625f,
                centerX - yawOffset,
                centerY + pitchOffset,
                player,
            )
            //? if < 1.21.8
            //graphics.pose().popPose()
            //?} else {
            /*GlStateManager.color4f(1f, 1f, 1f, 1f)
            SurvivalInventoryScreen.renderEntity(
                (width / 2f).toInt(),
                (height / 2f + 36f).toInt(),
                40,
                yawOffset,
                -pitchOffset,
                player,
            )
            *///?}
        } finally {
            playerPreviewPartialTick = -1f
            //? if = 1.8.9 {
            /*player.lastBodyYaw = lastBodyYaw
            player.lastYaw = lastYaw
            player.lastPitch = lastPitch
            *///?}
        }
    }
}
