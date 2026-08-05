package org.polyfrost.evergreenhud.client.hud

//? if > 1.8.9 {
import net.minecraft.network.protocol.common.ClientboundPingPacket
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
//?} else {
/*import net.minecraft.network.packet.s2c.play.WorldTimeS2CPacket as ClientboundSetTimePacket
import net.minecraft.network.packet.s2c.play.InventoryMenuConfirmS2CPacket
*///?}
import org.polyfrost.evergreenhud.client.ServerChangedEvent
import org.polyfrost.evergreenhud.client.utils.GenericNumberHud
import org.polyfrost.evergreenhud.client.utils.replace
import org.polyfrost.oneconfig.api.config.v1.annotations.Text
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.PacketEvent

class TpsHud : GenericNumberHud(
    title = "TPS",
    category = Category.INFO,
) {
    @Text(title = "Format String", description = "Use #tps for the current tps, #avg for the average, #low for the lowest. Average and lowest reset when you change server.")
    private var formatString = "#tps"

    private var lastUpdated = 0L

    private var useTickPings = false

    private val pingTimes = ArrayDeque<Long>()

    private var sampleCount = 0L
    private var sampleSum = 0.0
    private var lowest = 0f

    override fun setup() {
        super.setup()

        eventHandler { (ip, _, _): ServerChangedEvent ->
            useTickPings = isHypixel(ip)
            lastUpdated = 0L
            pingTimes.clear()
            resetStats()
        }

        eventHandler { (packet): PacketEvent.Receive ->
            when (packet) {
                is ClientboundSetTimePacket -> if (!useTickPings) onTimeUpdate()
                //? if > 1.8.9
                is ClientboundPingPacket -> if (useTickPings) onTickPing()
                //? if = 1.8.9
                //is InventoryMenuConfirmS2CPacket -> if (useTickPings && packet.menuId == 0 && !packet.accepted) onTickPing()
            }
        }

        if (isReal) {
            updateWhenChanged("formatString")
        }
    }

    private fun onTimeUpdate() {
        val now = System.currentTimeMillis()
        val previous = lastUpdated
        lastUpdated = now
        if (previous == 0L) return
        val timeTaken = now - previous
        if (timeTaken <= 0L) return
        record((20000f / timeTaken).coerceIn(0f, 20f))
    }

    private fun onTickPing() {
        val now = System.currentTimeMillis()
        pingTimes.addLast(now)

        while (pingTimes.size > 2 && now - pingTimes.first() > WINDOW_MS) {
            pingTimes.removeFirst()
        }

        val span = now - pingTimes.first()
        if (pingTimes.size < 2 || span <= 0L) return
        record(((pingTimes.size - 1) * 1000f / span).coerceIn(0f, 20f))
    }

    private fun record(tps: Float) {
        lowest = if (sampleCount == 0L) tps else minOf(lowest, tps)
        sampleCount++
        sampleSum += tps
        value = tps
        updateWithText(render(tps))
    }

    private fun render(tps: Float): String {
        val average = if (sampleCount == 0L) tps else (sampleSum / sampleCount).toFloat()
        return StringBuilder().append(formatString)
            .replace("#tps", format(tps))
            .replace("#avg", format(average))
            .replace("#low", format(if (sampleCount == 0L) tps else lowest))
            .toString()
    }

    private fun resetStats() {
        sampleCount = 0L
        sampleSum = 0.0
        lowest = 0f
    }

    private companion object {
        const val WINDOW_MS = 1000L

        fun isHypixel(ip: String?): Boolean {
            val host = ip?.substringBefore(':')?.trimEnd('.')?.lowercase() ?: return false
            return host == "hypixel.net" || host.endsWith(".hypixel.net")
        }
    }
}
