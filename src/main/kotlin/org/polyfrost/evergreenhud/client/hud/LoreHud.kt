package org.polyfrost.evergreenhud.client.hud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
//? if > 1.8.9 {
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
//?} else
//import org.polyfrost.oneconfig.internal.legacy.chat.Component
import net.minecraft.world.item.ItemStack
import org.polyfrost.evergreenhud.client.SelectedItemChangedEvent
import org.polyfrost.evergreenhud.client.utils.HudStyledLines
import org.polyfrost.evergreenhud.client.utils.StyledRun
import org.polyfrost.evergreenhud.client.utils.plainText
import org.polyfrost.evergreenhud.client.utils.toStyledRuns
import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox
import org.polyfrost.oneconfig.api.config.v1.annotations.Number
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.evergreenhud.client.utils.AutoHideTextHud

private const val AQUA = 0xFF55FFFF.toInt()
private const val GRAY = 0xFFAAAAAA.toInt()

private val PLACEHOLDER_LINES = listOf(
    listOf(StyledRun("Item Lore HUD", AQUA, bold = false, italic = false)),
    listOf(StyledRun("Hold an item with lore", GRAY, bold = false, italic = false)),
)

class LoreHud : AutoHideTextHud(
    id = "lore.json",
    title = "Item Lore",
    category = Category.INFO,
    prefix = "",
) {
    @Switch(title = "Show Item Name")
    var showName = true

    @Checkbox(title = "Remove Empty Lines")
    var removeEmptyLines = true

    @Number(title = "Max Lines", min = 0f, max = 50f)
    var maxLines = 0

    private val ItemStack.isNameShown: Boolean
        //~ if = 1.8.9 'has(DataComponents.CUSTOM_NAME)' -> 'hasCustomHoverName()'
        get() = has(DataComponents.CUSTOM_NAME)

    private var currentLines: List<List<StyledRun>> = emptyList()
    private var linesState: MutableState<List<List<StyledRun>>> = mutableStateOf(emptyList())

    override fun defaultPosition(): Pair<Float, Float> = 0f to 0f

    override fun setup() {
        super.setup()
        if (isReal) {
            updateWhenChanged("showName")
            updateWhenChanged("removeEmptyLines")
            updateWhenChanged("maxLines")
            eventHandler { (item): SelectedItemChangedEvent ->
                theItem = item
            }
        }
    }

    var theItem: ItemStack? = null
        set(value) {
            val old = field
            if (old == null && value == null) return
            if (old != null && value != null && ItemStack.matches(old, value)) return
            field = value
            updateAndRecalculate()
        }

    @Composable
    override fun Content() = HudStyledLines(linesState.value)

    override fun update(): Boolean {
        val lore = loreLines()
        autoHidden = isReal && lore.isEmpty()
        currentLines = lore.ifEmpty { PLACEHOLDER_LINES }

        val result = super.update()
        linesState.value = currentLines
        return result
    }

    override fun getText(): String = currentLines.joinToString("\n") { it.plainText() }

    private fun loreLines(): List<List<StyledRun>> {
        val item = theItem ?: return emptyList()
        val out = ArrayList<List<StyledRun>>()

        if (showName && item.isNameShown) {
            //~ if = 1.8.9 'item.hoverName' -> 'item.displayName'
            val name = item.hoverName.toStyledRuns()
            if (name.plainText().isNotEmpty()) out.add(name)
        }

        var i = 0
        item.forEachLore { line ->
            if (maxLines in 1..i) return@forEachLore
            if (removeEmptyLines && line.plainText().isBlank()) return@forEachLore
            out.add(line)
            i++
        }

        return out
    }

    private inline fun ItemStack.forEachLore(consumer: (List<StyledRun>) -> Unit) {
        //? if > 1.8.9 {
        val lore = this.get(DataComponents.LORE) ?: return
        for (line: Component in lore.lines) {
            consumer(line.toStyledRuns())
        }
        //?} else {
        /*val display = nbt?.getCompound("display") ?: return
        val lore = display.getList("Lore", 8)
        for (i in 0 until lore.size()) {
            consumer(Component.fromLegacy(lore.getString(i)).toStyledRuns())
        }
        *///?}
    }

    override fun clone(): Hud = (super.clone() as LoreHud).also {
        it.linesState = mutableStateOf(emptyList())
    }
}
