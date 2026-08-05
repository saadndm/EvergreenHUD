package org.polyfrost.evergreenhud.client.hud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.polyfrost.evergreenhud.client.utils.Facing
import org.polyfrost.evergreenhud.client.utils.GenericNumberHud
import org.polyfrost.evergreenhud.client.utils.HudStyledLines
import org.polyfrost.evergreenhud.client.utils.StyledRun
import org.polyfrost.evergreenhud.client.utils.cameraYaw
import org.polyfrost.oneconfig.api.config.v1.annotations.Checkbox
import org.polyfrost.oneconfig.api.config.v1.annotations.RadioButton
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.utils.v1.dsl.mc

private const val NO_SIGN = ' '

private const val CELL_SEP = '\u0000'

class PositionHud : GenericNumberHud(
    title = "Position",
    category = Category.INFO,
    prefix = ""
) {
    init {
        textAlign = 0
    }

    @RadioButton(
        title = "Mode",
        options = ["Vertical", "Horizontal"]
    )
    var displayMode = 0

    @Switch(title = "Show Axis")
    var showAxis = true

    @Switch(title = "Show Direction")
    var showDirection = false

    @Switch(title = "Align Direction")
    var alignDirection = true

    @Checkbox(title = "Show X")
    var showX = true

    @Checkbox(title = "Show Y")
    var showY = true

    @Checkbox(title = "Show Z")
    var showZ = true

    @Checkbox(title = "Show Yaw")
    var showYaw = false

    @Checkbox(title = "Show Pitch")
    var showPitch = false

    private val facing get() = Facing.parseExact(cameraYaw ?: 0f)
    private var px = 0.0
    private var py = 0.0
    private var pz = 0.0
    private var yaw = 0.0
    private var pitch = 0.0

    private var linesState: MutableState<List<List<StyledRun>>> = mutableStateOf(emptyList())
    private var alignState: MutableState<Boolean> = mutableStateOf(false)

    override fun setup() {
        super.setup()
        eventHandler { _: TickEvent.End ->
            val player = mc.player ?: return@eventHandler
            //~ if = 1.8.9 'mc.cameraEntity' -> 'mc.camera'
            val camera = mc.cameraEntity ?: player
            this.px = player.x
            this.py = player.y
            this.pz = player.z
            //~ if = 1.8.9 'camera.yRot' -> 'camera.yaw'
            this.yaw = Facing.wrapDegrees(camera.yRot).toDouble()
            //~ if = 1.8.9 'camera.xRot' -> 'camera.pitch'
            this.pitch = camera.xRot.toDouble()
            updateAndRecalculate()
        }

        if (isReal) {
            updateWhenChanged("showDirection")
            updateWhenChanged("alignDirection")
            updateWhenChanged("showX")
            updateWhenChanged("showY")
            updateWhenChanged("showZ")
            updateWhenChanged("showYaw")
            updateWhenChanged("showPitch")
            updateWhenChanged("showAxis")
            updateWhenChanged("displayMode")

            hideIf("alignDirection") { !showDirection || displayMode != 0 }
        }
    }

    @Composable
    override fun Content() = HudStyledLines(linesState.value, alignColumns = alignState.value)

    override fun update(): Boolean {
        val raw = createText()
        currentText = raw.replace(CELL_SEP.toString(), "")
        val result = super.update()

        linesState.value = concat(prefix, raw, suffix).lines().map { line ->
            line.split(CELL_SEP).map { StyledRun(it, null, false, false) }
        }
        alignState.value = alignDirection && showDirection && displayMode == 0
        return result
    }

    private fun entry(axis: Char, value: Double, sign: Char): String = buildString {
        if (showAxis) {
            append(axis).append(": ")
        }

        append(format(value))
        if (showDirection && sign != NO_SIGN) {
            append(CELL_SEP).append("  (").append(sign).append(')')
        }
    }

    private fun angleEntry(label: String, value: Double): String = buildString {
        if (showAxis) {
            append(label).append(": ")
        }
        append(format(value))
    }

    private fun createText(): String {
        val facing = this.facing
        val entries = ArrayList<String>(5)

        if (showX) {
            entries.add(entry('X', px, if (facing.isEast) '+' else if (facing.isWest) '-' else NO_SIGN))
        }

        if (showY) {
            entries.add(entry('Y', py, NO_SIGN))
        }

        if (showZ) {
            entries.add(entry('Z', pz, if (facing.isSouth) '+' else if (facing.isNorth) '-' else NO_SIGN))
        }

        if (showYaw) {
            entries.add(angleEntry("Yaw", yaw))
        }

        if (showPitch) {
            entries.add(angleEntry("Pitch", pitch))
        }

        return entries.joinToString(if (displayMode == 0) "\n" else ", ")
    }

    override fun clone(): Hud = (super.clone() as PositionHud).also {
        it.linesState = mutableStateOf(emptyList())
        it.alignState = mutableStateOf(false)
    }

    override fun defaultPosition(): Pair<Float, Float> = 10f to 30f
}
