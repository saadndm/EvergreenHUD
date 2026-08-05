package org.polyfrost.evergreenhud.client.hud

//? if = 1.8.9 {
/*import net.minecraft.client.network.ServerAddress
import net.minecraft.client.network.handler.ClientQueryPacketHandler
import net.minecraft.network.Connection
import net.minecraft.network.NetworkProtocol
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket
import net.minecraft.network.packet.c2s.query.PingC2SPacket
import net.minecraft.network.packet.c2s.query.ServerStatusC2SPacket
import net.minecraft.network.packet.s2c.query.PingS2CPacket
import net.minecraft.network.packet.s2c.query.ServerStatusS2CPacket
import net.minecraft.text.LiteralText
import net.minecraft.text.Text as MinecraftText
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.utils.v1.Multithreading
import java.net.InetAddress
*///?}
import org.polyfrost.evergreenhud.client.ServerChangedEvent
import org.polyfrost.evergreenhud.client.utils.CachedTextHud
import org.polyfrost.evergreenhud.client.utils.replace
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Text
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.WorldEvent
//? if > 1.8.9
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import kotlin.time.Duration.Companion.milliseconds

class PingHud : CachedTextHud(
    title = "Ping",
    category = Category.INFO,
) {
    companion object {
        //? if > 1.8.9 {
        private const val MS_PER_TICK = 50

        @JvmStatic
        fun sampleIntervalTicks(): Int {
            var interval = -1
            for (hud in HudManager.activeInstances) {
                if (hud !is PingHud || hud.hidden) continue
                if (interval == -1 || hud.sampleIntervalTicks < interval) interval = hud.sampleIntervalTicks
            }
            return interval
        }
        //?} else {
        /*private const val TIMEOUT_MS = 5_000L
        private fun now() = System.nanoTime() / 1_000_000L
        *///?}
    }

    //? if > 1.8.9 {
    @Slider(title = "Update Rate (ms)", min = 250F, max = 5000F, step = 250F)
    var updateRate = 500
    //?} else {
    /*@Slider(title = "Update Rate (ms)", min = 1000F, max = 5000F, step = 250F)
    var updateRate = 1000
    *///?}

    @Text(title = "Format String", description = "Use #ping for the current ping, #avg for the average, #high for the highest. Average and highest reset when you change server.")
    private var formatString = "#pingms"

    private var sampleCount = 0L
    private var sampleSum = 0.0
    private var highest = 0L

    //? if > 1.8.9 {
    private val sampleIntervalTicks get() = (updateRate / MS_PER_TICK).coerceAtLeast(1)
    //?} else {
    /*@Volatile private var ping: Long? = null
    @Volatile private var connection: Connection? = null
    private var statusAddress: String? = null
    @Volatile private var requestStarted = 0L
    private var nextRequest = 0L
    @Volatile private var generation = 0L
    *///?}

    override fun setup() {
        super.setup()
        eventHandler { _: WorldEvent.Load ->
            //~ if = 1.8.9 'mc.hasSingleplayerServer()' -> 'mc.isIntegratedServerRunning'
            autoHidden = mc.hasSingleplayerServer()
        }

        eventHandler { (ip): ServerChangedEvent ->
            //? if = 1.8.9 {
            /*cancelPing()
            statusAddress = ip
            *///?}
            resetStats()
        }

        //? if = 1.8.9 {
        /*eventHandler<TickEvent.Start> {
            connection?.let { if (it.isConnected) it.tick() else it.handleDisconnection() }
            val now = now()
            if (requestStarted != 0L && now - requestStarted >= TIMEOUT_MS) finish(generation, null)
            if (isReal && !hidden && requestStarted == 0L && now >= nextRequest) {
                statusAddress?.let(::requestPing)
            }
        }
        *///?}

        if (isReal) {
            updateWhenChanged("formatString")
        }
    }

    override fun updateFrequency(): Long {
        return updateRate.milliseconds.inWholeNanoseconds
    }

    override fun getText(): String {
        //? if > 1.8.9 {
        val ping = if (isReal) measuredPing() ?: serverReportedPing() else null
        //?} else
        //val ping = if (isReal) ping else null
        if (ping == null) return formatString.replace("#ping", "-1").replace("#avg", "-1").replace("#high", "-1")
        //? if > 1.8.9
        recordSample(ping)
        return formatPing(ping)
    }

    private fun recordSample(ping: Long) {
        highest = if (sampleCount == 0L) ping else maxOf(highest, ping)
        sampleCount++
        sampleSum += ping
    }

    private fun formatPing(ping: Long): String {
        val average = (sampleSum / sampleCount).toLong()
        return StringBuilder().append(formatString)
            .replace("#ping", ping.toString())
            .replace("#avg", average.toString())
            .replace("#high", highest.toString())
            .toString()
    }

    private fun resetStats() {
        sampleCount = 0L
        sampleSum = 0.0
        highest = 0L
    }

    //? if > 1.8.9 {
    private fun measuredPing(): Long? {
        if (mc.connection == null) return null
        val logger = mc.debugOverlay?.pingLogger ?: return null
        val size = logger.size()
        return if (size == 0) null else logger.get(size - 1)
    }

    private fun serverReportedPing(): Long? =
        mc.player?.uuid?.let { mc.connection?.getPlayerInfo(it)?.latency?.toLong() }
    //?} else {
    /*private fun requestPing(addressString: String) {
        requestStarted = now()
        val requestGeneration = generation
        Multithreading.submit {
            try {
                val address = ServerAddress.parse(addressString)
                val connection = Connection.connect(InetAddress.getByName(address.address), address.port, false)
                synchronized(this) {
                    if (requestGeneration != generation) {
                        connection.disconnect(LiteralText("Ping cancelled"))
                        return@submit
                    }

                    connection.listener = PingHandler(connection, requestGeneration)
                    this.connection = connection
                    connection.send(HandshakeC2SPacket(47, address.address, address.port, NetworkProtocol.STATUS))
                    connection.send(ServerStatusC2SPacket())
                }
            } catch (_: Exception) {
                finish(requestGeneration, null)
            }
        }
    }

    @Synchronized
    private fun finish(requestGeneration: Long, result: Long?) {
        if (requestGeneration != generation) return
        if (result != null) {
            ping = result
            recordSample(result)
        }
        generation++
        nextRequest = now() + updateRate
        closeConnection()
        requestStarted = 0L
    }

    @Synchronized
    private fun cancelPing() {
        generation++
        ping = null
        nextRequest = 0L
        closeConnection()
        requestStarted = 0L
    }

    private fun closeConnection() {
        connection?.let { if (it.isConnected) it.disconnect(LiteralText("Ping complete")) }
        connection = null
    }

    private inner class PingHandler(
        private val connection: Connection,
        private val requestGeneration: Long,
    ) : ClientQueryPacketHandler {
        private var started = 0L

        override fun handleServerStatus(packet: ServerStatusS2CPacket) {
            started = now()
            connection.send(PingC2SPacket(started))
        }

        override fun handlePing(packet: PingS2CPacket) = finish(requestGeneration, now() - started)

        override fun onDisconnect(reason: MinecraftText) = finish(requestGeneration, null)
    }
    *///?}
}
