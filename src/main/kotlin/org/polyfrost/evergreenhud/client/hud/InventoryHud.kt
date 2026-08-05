package org.polyfrost.evergreenhud.client.hud

//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.GuiGraphics
*///?} else {
/*import net.minecraft.client.gui.GuiElement
import net.minecraft.client.render.platform.Lighting
*///?}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.evergreenhud.client.hooks.EnderChestTracker
//? if > 1.8.9 {
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemContainerContents
import org.polyfrost.evergreenhud.client.hooks.ShulkerPreview
import org.polyfrost.evergreenhud.client.hooks.heldShulkerBox
import org.polyfrost.evergreenhud.client.hooks.shulkerContents
import org.polyfrost.oneconfig.api.config.v1.annotations.Keybind
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.ui.v1.keybind.KeybindHelper
import org.polyfrost.oneconfig.api.ui.v1.keybind.OneConfigKeybind
//?}
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc

class InventoryHud : LegacyHud(
    id = "inventory.json",
    title = "Inventory",
    category = Category.PLAYER,
), HudBackground {
    private companion object {
        private const val PLAYER = 0
        private const val ENDER_CHEST = 1
        //? if > 1.8.9
        private const val HELD_SHULKER = 2

        private const val COLS = 9
        private const val ROWS = 3
        private const val SLOT = 18
        private const val ITEM = 16
        private const val EDGE = 8

        //? if > 1.8.9 {
        private val exampleShulker by lazy {
            ItemStack(Items.SHULKER_BOX).apply {
                set(
                    DataComponents.CONTAINER,
                    ItemContainerContents.fromItems(
                        listOf(
                            ItemStack(Items.DIAMOND, 32),
                            ItemStack(Items.ENCHANTED_BOOK),
                            ItemStack(Items.GOLDEN_APPLE, 8),
                            ItemStack(Items.DIAMOND_PICKAXE),
                        ),
                    ),
                )
            }
        }
        //?}

        private val exampleContents by lazy {
            List(ROWS * COLS) { i ->
                when (i) {
                    0 -> ItemStack(Items.DIAMOND, 32)
                    1 -> ItemStack(Items.ENCHANTED_BOOK)
                    9 -> ItemStack(Items.GOLDEN_APPLE, 8)
                    11 -> ItemStack(Items.DIAMOND_PICKAXE)
                    //~ if = 1.8.9 'ItemStack.EMPTY' -> 'null'
                    else -> ItemStack.EMPTY
                }
            }
        }
    }

    //? if > 1.8.9 {
    @RadioButton(title = "Inventory", options = ["Player", "Ender Chest", "Held Shulker"])
    //?} else
    //@RadioButton(title = "Inventory", options = ["Player", "Ender Chest"])
    var type = PLAYER

    @Switch(title = "Show Title")
    var showTitle = true

    @Switch(title = "Background")
    override var background = true

    @Color(title = "Background Color")
    override var backgroundColor = PolyColor(0x90000000.toInt())

    //? if > 1.8.9 {
    @Keybind(
        title = "Pin Shulker Preview",
        description = "Held Shulker only. Keeps the shulker's contents on screen after you stop holding it. Press again to unpin.",
    )
    var pinKey: OneConfigKeybind = KeybindHelper.builder()
        .key(InputConstants.KEY_V)
        .inScreens()
        .action { pressed: Boolean -> if (pressed) ShulkerPreview.togglePin(); true }
        .register()

    private fun shulker(): ItemStack? =
        if (!isReal) exampleShulker else ShulkerPreview.pinnedStack ?: mc.player?.heldShulkerBox()
    //?}

    private val visible: Boolean
        //? if > 1.8.9 {
        get() = type != HELD_SHULKER || HudManager.isEditing || shulker() != null
        //?} else
        //get() = true

    override val width get() = if (visible) 176f else 0f
    override val height get() = if (!visible) 0f else if (showTitle) 92f else 78f

    override fun defaultPosition(): Pair<Float, Float> = 0f to 0f

    override fun setup() {
        super.setup()
        staticWidth = true
        if (isReal) {
            hideIf("backgroundColor") { !background }
        }
    }

    override fun update() = false

    private fun titleText(): String = when (type) {
        PLAYER -> "Inventory"
        ENDER_CHEST -> "Ender Chest"
        //? if > 1.8.9 {
        else -> shulker()?.hoverName?.string ?: "Shulker Box"
        //?} else
        //else -> "Inventory"
    }

    //~ if = 1.8.9 'List<ItemStack>?' -> 'List<ItemStack?>?'
    private fun contents(): List<ItemStack>? = when (type) {
        //? if > 1.8.9
        HELD_SHULKER -> shulker()?.shulkerContents()
        ENDER_CHEST -> if (!isReal) exampleContents else EnderChestTracker.contents()
        else -> {
            val inv = mc.player?.inventory
            // slot 0..8 is the hotbar, which the main inventory grid does not show
            inv?.let {
                List(ROWS * COLS) { i ->
                    //? if > 1.8.9 {
                    if (COLS + i < it.containerSize) it.getItem(COLS + i) else ItemStack.EMPTY
                    //?} else
                    //if (COLS + i < it.size) it.getItem(COLS + i) else null
                }
            }
        }
    }

    //? if > 1.8.9 {
    override fun render(graphics: GuiGraphics) {
    //?} else
    //override fun render() {
        if (!visible) return

        backgroundArgb?.let {
            //~ if = 1.8.9 'graphics' -> 'GuiElement'
            graphics.fill(0, 0, width.toInt(), height.toInt(), it)
        }

        //~ if = 1.8.9 'mc.font' -> 'mc.textRenderer'
        val font = mc.font

        if (showTitle) {
            //? if >= 26 {
            graphics.text(mc.font, titleText(), EDGE, 6, 0xFFFFFFFF.toInt())
            //?} elif > 1.8.9 {
            /*graphics.drawString(mc.font, titleText(), EDGE, 6, 0xFFFFFFFF.toInt())
            *///?} else
            //font.drawWithShadow(titleText(), EDGE.toFloat(), 6f, 0xFFFFFFFF.toInt())
        }

        val items = contents() ?: return
        val top = if (showTitle) 20 else 6
        //? if > 1.8.9
        val slots = if (type == HELD_SHULKER && isReal && !HudManager.isEditing) ArrayList<ShulkerPreview.Slot>(items.size) else null

        //? if = 1.8.9 {
        /*// 1.8.9's item renderer expects callers to configure and restore GUI lighting.
        Lighting.turnOnGui()
        *///?}
        for (i in items.indices) {
            val item = items[i]
            //~ if = 1.8.9 'item.isEmpty' -> 'item == null'
            if (item.isEmpty) continue
            val itemX = EDGE + (i % COLS) * SLOT
            val itemY = top + (i / COLS) * SLOT
            //? if > 26 {
            graphics.item(item, itemX, itemY)
            graphics.itemDecorations(font, item, itemX, itemY)
            //?} elif > 1.8.9 {
            /*graphics.renderItem(item, itemX, itemY)
            graphics.renderItemDecorations(font, item, itemX, itemY)
            *///?} else {
            /*mc.itemRenderer.renderGuiItem(item, itemX, itemY)
            mc.itemRenderer.renderGuiItemDecoration(font, item, itemX, itemY)
            *///?}
            //? if > 1.8.9
            slots?.add(ShulkerPreview.Slot(x + itemX * effectiveScale, y + itemY * effectiveScale, ITEM * effectiveScale, item))
        }
        //? if = 1.8.9
        //Lighting.turnOff()

        //? if > 1.8.9
        slots?.let { ShulkerPreview.publishSlots(it) }
    }
}
