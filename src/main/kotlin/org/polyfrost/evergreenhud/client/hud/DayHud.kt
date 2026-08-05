package org.polyfrost.evergreenhud.client.hud

import org.polyfrost.evergreenhud.client.utils.CachedTextHud
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import kotlin.time.Duration.Companion.seconds

class DayHud : CachedTextHud(
    title = "Day",
    category = Category.INFO,
    defaultText = "0"
) {
    override fun getText(): String {
        //? if > 1.8.9 {
        //~ if < 26.1 'level?.overworldClockTime?' -> 'level?.dayTime?'
        return mc.level?.overworldClockTime?.div(24000L)?.toString() ?: "0"
        //?} else
        //return mc.world.timeOfDay.div(24000L).toString() ?: "0"
    }

    override fun updateFrequency(): Long = 1.seconds.inWholeNanoseconds
}
