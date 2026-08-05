package org.polyfrost.evergreenhud.client.hud

//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics
//?} elif > 1.8.9 {
/*import net.minecraft.client.gui.GuiGraphics
*///?} else {
/*import net.minecraft.client.gui.GuiElement
import net.minecraft.client.render.platform.Lighting
*///?}
//? if > 1.8.9
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.polyfrost.compose.render.PolyColor
import org.polyfrost.oneconfig.api.config.v1.annotations.Color
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown
import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

class ArmorHud : LegacyHud(
    id = "armor.json",
    title = "Armor Status",
    category = Category.PLAYER,
), HudBackground {
    private companion object {
        private const val ICON = 16
        private const val TEXT_GAP = 2

        private const val HORIZONTAL = 0
        private const val RIGHT = 1

        private const val DURABILITY = 1
        private const val DURABILITY_PERCENT = 2
        private const val NAME = 3

        private val exampleHelmet by lazy { ItemStack(Items.DIAMOND_HELMET) }
        private val exampleChestplate by lazy { ItemStack(Items.DIAMOND_CHESTPLATE) }
        private val exampleLeggings by lazy { ItemStack(Items.DIAMOND_LEGGINGS) }
        private val exampleBoots by lazy { ItemStack(Items.DIAMOND_BOOTS) }
        private val exampleMainHand by lazy { ItemStack(Items.DIAMOND_SWORD) }
        //? if > 1.8.9
        private val exampleOffhand by lazy { ItemStack(Items.SHIELD) }
    }

    @Switch(title = "Helmet")
    var showHelmet = true

    @Switch(title = "Chestplate")
    var showChestplate = true

    @Switch(title = "Leggings")
    var showLeggings = true

    @Switch(title = "Boots")
    var showBoots = true

    @Switch(title = "Main Hand")
    var showMainHand = true

    //? if > 1.8.9 {
    @Switch(title = "Off Hand")
    var showOffhand = true
    //?}

    @Slider(title = "Padding", min = 0F, max = 20F, step = 1F)
    var padding = 5f

    @RadioButton(title = "Direction", options = ["Horizontal", "Vertical"])
    var direction = HORIZONTAL

    @Switch(title = "Reversed")
    var reversed = false

    @Switch(title = "Item Decorations", description = "Vanilla durability bar and stack count.")
    var showDecorations = true

    @Switch(title = "Bow Arrow Count", description = "Show the number of arrows you carry on a held bow, where the stack count would be.")
    var showArrowCount = true

    @Dropdown(title = "Extra Info", options = ["None", "Durability", "Durability %", "Item Name"])
    var extraInfo = 0

    @RadioButton(title = "Text Position", options = ["Left", "Right"])
    var textPosition = RIGHT

    @Switch(title = "Dynamic Durability Color", description = "Fade the durability text between two colors as the item wears down.")
    var dynamicColor = false

    @Color(title = "Full Durability Color")
    var fullDurabilityColor = PolyColor(0xFFFFFFFF.toInt())

    @Color(title = "Empty Durability Color")
    var emptyDurabilityColor = PolyColor(0xFFFF5555.toInt())

    @Switch(title = "Background")
    override var background = true

    @Color(title = "Background Color")
    override var backgroundColor = PolyColor(0x80000000.toInt())

    private class Entry(
        val stack: ItemStack,
        val text: String,
        val textColor: Int,
        val iconX: Int,
        val iconY: Int,
        val textX: Int,
        val textY: Int,
    )

    private var layout: List<Entry> = emptyList()
    private var actualW = ICON.toFloat()
    private var actualH = ICON.toFloat()

    override val width get() = actualW
    override val height get() = actualH

    override fun defaultPosition(): Pair<Float, Float> = 0f to 0f

    override fun setup() {
        super.setup()
        if (isReal) {
            hideIf("backgroundColor") { !background }
            hideIf("fullDurabilityColor") { !dynamicColor }
            hideIf("emptyDurabilityColor") { !dynamicColor }
        }
    }

    override fun update(): Boolean {
        recompute()
        return false
    }

    private fun currentItems(): List<ItemStack> {
        var list = if (isReal) equippedItems() else exampleItems()
        // with nothing equipped the hud would be empty, leaving nothing to drag in the editor
        if (list.isEmpty() && HudManager.isEditing) list = exampleItems()
        if (reversed) list.reverse()
        return list
    }

    private fun exampleItems(): ArrayList<ItemStack> {
        val list = ArrayList<ItemStack>(6)
        if (showHelmet) list.add(exampleHelmet)
        if (showChestplate) list.add(exampleChestplate)
        if (showLeggings) list.add(exampleLeggings)
        if (showBoots) list.add(exampleBoots)
        if (showMainHand) list.add(exampleMainHand)
        //? if > 1.8.9
        if (showOffhand) list.add(exampleOffhand)
        return list
    }

    private fun equippedItems(): ArrayList<ItemStack> {
        val list = ArrayList<ItemStack>(6)
        val player = mc.player ?: return list
        //? if > 1.8.9 {
        fun add(slot: EquipmentSlot) = player.getItemBySlot(slot).let { if (!it.isEmpty) list.add(it) }
        if (showHelmet) add(EquipmentSlot.HEAD)
        if (showChestplate) add(EquipmentSlot.CHEST)
        if (showLeggings) add(EquipmentSlot.LEGS)
        if (showBoots) add(EquipmentSlot.FEET)
        if (showMainHand) add(EquipmentSlot.MAINHAND)
        if (showOffhand) add(EquipmentSlot.OFFHAND)
        //?} else {
        /*fun add(stack: ItemStack?) = stack?.let { list.add(it) }
        if (showHelmet) add(player.getArmor(3))
        if (showChestplate) add(player.getArmor(2))
        if (showLeggings) add(player.getArmor(1))
        if (showBoots) add(player.getArmor(0))
        if (showMainHand) add(player.itemInHand)
        *///?}
        return list
    }

    private fun infoText(stack: ItemStack): String = when (extraInfo) {
        //? if > 1.8.9 {
        DURABILITY -> if (stack.isDamageableItem) (stack.maxDamage - stack.damageValue).toString() else ""
        DURABILITY_PERCENT -> if (stack.isDamageableItem) "${durabilityPercent(stack)}%" else ""
        NAME -> stack.hoverName.string
        //?} else {
        /*DURABILITY -> if (stack.isDamageable) (stack.maxDamage - stack.damage).toString() else ""
        DURABILITY_PERCENT -> if (stack.isDamageable) "${durabilityPercent(stack)}%" else ""
        NAME -> stack.hoverName
        *///?}
        else -> ""
    }

    private fun durabilityPercent(stack: ItemStack): Int =
        //~ if = 1.8.9 'stack.damageValue' -> 'stack.damage'
        ceil((stack.maxDamage - stack.damageValue).toFloat() / stack.maxDamage.toFloat() * 100f).toInt()

    private fun arrowCount(): Int {
        if (!isReal) return 64
        val player = mc.player ?: return 0
        val inv = player.inventory
        var count = 0
        //~ if = 1.8.9 'inv.containerSize' -> 'inv.size'
        for (i in 0 until inv.containerSize) {
            val s = inv.getItem(i)
            //? if > 1.8.9 {
            if (s.item == Items.ARROW || s.item == Items.SPECTRAL_ARROW || s.item == Items.TIPPED_ARROW) count += s.count
            //?} else
            //if (s != null && s.item == Items.ARROW) count += s.size
        }
        return count
    }

    private fun durabilityColor(percent: Int): Int {
        val p = percent.coerceIn(0, 100) / 100f
        val empty = emptyDurabilityColor.argb
        val full = fullDurabilityColor.argb
        fun lerp(shift: Int): Int {
            val from = (empty shr shift) and 0xFF
            val to = (full shr shift) and 0xFF
            return (from + (to - from) * p).roundToInt().coerceIn(0, 255) shl shift
        }
        return lerp(24) or lerp(16) or lerp(8) or lerp(0)
    }

    private fun recompute() {
        val stacks = currentItems()
        if (stacks.isEmpty()) {
            layout = emptyList()
            actualW = 0f
            actualH = 0f
            return
        }

        //~ if = 1.8.9 'mc.font' -> 'mc.textRenderer'
        val font = mc.font
        //~ if = 1.8.9 'font.lineHeight' -> 'font.fontHeight'
        val textY = ((ICON - font.lineHeight) / 2f).roundToInt()
        val pad = padding.toInt()
        val entries = ArrayList<Entry>(stacks.size)

        var cursorX = 0
        var cursorY = 0
        var maxCell = 0

        for (stack in stacks) {
            val text = infoText(stack)
            //~ if = 1.8.9 'font.width(' -> 'font.getWidth('
            val textW = if (text.isEmpty()) 0 else font.width(text)
            val textPart = if (textW > 0) TEXT_GAP + textW else 0
            val cellW = ICON + textPart

            //~ if = 1.8.9 'stack.isDamageableItem' -> 'stack.isDamageable'
            val color = if (dynamicColor && stack.isDamageableItem &&
                (extraInfo == DURABILITY || extraInfo == DURABILITY_PERCENT)
            ) {
                durabilityColor(durabilityPercent(stack))
            } else {
                textColor
            }

            val originX: Int
            val originY: Int
            if (direction == HORIZONTAL) {
                originX = cursorX
                originY = 0
            } else {
                originX = 0
                originY = cursorY
            }

            val iconX: Int
            val textX: Int
            if (textPosition == RIGHT) {
                iconX = originX
                textX = originX + ICON + TEXT_GAP
            } else {
                textX = originX
                iconX = originX + textPart
            }

            entries.add(Entry(stack, text, color, iconX, originY, textX, originY + textY))

            maxCell = max(maxCell, cellW)
            if (direction == HORIZONTAL) {
                cursorX += cellW + pad
            } else {
                cursorY += ICON + pad
            }
        }

        layout = entries
        if (direction == HORIZONTAL) {
            actualW = (cursorX - pad).toFloat()
            actualH = ICON.toFloat()
        } else {
            actualW = maxCell.toFloat()
            actualH = (cursorY - pad).toFloat()
        }
    }

    //? if > 1.8.9 {
    override fun render(graphics: GuiGraphics) {
    //?} else
    //override fun render() {
        val entries = layout
        if (entries.isEmpty()) return

        backgroundArgb?.let {
            //~ if = 1.8.9 'graphics' -> 'GuiElement'
            graphics.fill(0, 0, actualW.toInt(), actualH.toInt(), it)
        }

        //~ if = 1.8.9 'mc.font' -> 'mc.textRenderer'
        val font = mc.font
        //? if = 1.8.9 {
        /*// 1.8.9's item renderer expects callers to configure and restore GUI lighting.
        Lighting.turnOnGui()
        *///?}
        for (e in entries) {
            //? if >= 26.1 {
            graphics.item(e.stack, e.iconX, e.iconY)
            if (showDecorations) graphics.itemDecorations(font, e.stack, e.iconX, e.iconY)
            //?} elif > 1.8.9 {
            /*graphics.renderItem(e.stack, e.iconX, e.iconY)
            if (showDecorations) graphics.renderItemDecorations(font, e.stack, e.iconX, e.iconY)
            *///?} else {
            /*mc.itemRenderer.renderGuiItem(e.stack, e.iconX, e.iconY)
            if (showDecorations) mc.itemRenderer.renderGuiItemDecoration(font, e.stack, e.iconX, e.iconY)
            *///?}

            //? if > 1.8.9 {
            if (showArrowCount && (e.stack.item == Items.BOW || e.stack.item == Items.CROSSBOW)) {
            //?} else
            //if (showArrowCount && e.stack.item == Items.BOW) {
                val count = arrowCount().toString()
                //~ if = 1.8.9 'font.width(' -> 'font.getWidth('
                val cx = e.iconX + ICON - font.width(count) + 1
                //~ if = 1.8.9 'font.lineHeight' -> 'font.fontHeight'
                val cy = e.iconY + ICON - font.lineHeight + 2
                //? if >= 26.1 {
                graphics.text(font, count, cx, cy, 0xFFFFFFFF.toInt())
                //?} elif > 1.8.9 {
                /*graphics.drawString(font, count, cx, cy, 0xFFFFFFFF.toInt())
                *///?} else
                //font.drawWithShadow(count, cx.toFloat(), cy.toFloat(), 0xFFFFFFFF.toInt())
            }

            if (e.text.isNotEmpty()) {
                //? if >= 26.1 {
                graphics.text(font, e.text, e.textX, e.textY, e.textColor)
                //?} elif > 1.8.9 {
                /*graphics.drawString(font, e.text, e.textX, e.textY, e.textColor)
                *///?} else
                //font.drawWithShadow(e.text, e.textX.toFloat(), e.textY.toFloat(), e.textColor)
            }
        }
        //? if = 1.8.9
        //Lighting.turnOff()
    }
}
