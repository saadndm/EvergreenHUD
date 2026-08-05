package org.polyfrost.evergreenhud.client.utils

//? if > 1.8.9 {
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.util.Optional
//?} else {
/*import net.minecraft.text.Text as Component
import org.polyfrost.oneconfig.internal.legacy.chat.Style
*///?}

data class StyledRun(
    val text: String,
    val argb: Int?,
    val bold: Boolean,
    val italic: Boolean,
)

fun Component.toStyledRuns(): List<StyledRun> {
    val runs = ArrayList<StyledRun>()
    //? if > 1.8.9 {
    visit({ style: Style, text: String ->
        if (text.isNotEmpty()) {
            runs.add(
                StyledRun(
                    text = text,
                    argb = style.color?.let { 0xFF000000.toInt() or it.value },
                    bold = style.isBold,
                    italic = style.isItalic,
                )
            )
        }
        Optional.empty<Unit>()
    }, Style.EMPTY)
    //?} else {
    /*for (part in this) {
        val text = part.content
        val style = part.style
        if (text.isNotEmpty()) {
            runs.add(
                StyledRun(
                    text = text,
                    argb = Style.fromLegacy(style).rgbColor?.let { 0xFF000000.toInt() or it },
                    bold = style.isBold,
                    italic = style.isItalic,
                )
            )
        }
    }
    *///?}
    return runs
}

fun List<StyledRun>.plainText(): String = joinToString("") { it.text }
