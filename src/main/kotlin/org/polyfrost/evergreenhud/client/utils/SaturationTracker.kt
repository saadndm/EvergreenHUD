// Derived from AppleSkin (https://github.com/squeek502/AppleSkin), made by squeek502.
// AppleSkin is licensed under the Unlicense (public domain).

package org.polyfrost.evergreenhud.client.utils

//? if > 1.8.9 {
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.polyfrost.oneconfig.api.event.v1.events.PacketEvent
import kotlin.math.roundToInt
import kotlin.math.sqrt
//?} else
//import net.minecraft.world.entity.player.Player as Player
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.food.FoodData
import org.polyfrost.evergreenhud.client.SaturationChangedEvent
import org.polyfrost.oneconfig.api.event.v1.EventManager
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max
import kotlin.math.min

object SaturationTracker {
    private const val EXHAUSTION_PER_SATURATION = 4.0f
    private const val MAX_EXHAUSTION = 40.0f

    private const val FAST_REGEN_INTERVAL = 10
    private const val SLOW_REGEN_INTERVAL = 80
    private const val FAST_REGEN_MAX_EXHAUSTION = 6.0f
    //~ if = 1.8.9 '6.0f' -> '3.0f'
    private const val SLOW_REGEN_EXHAUSTION = 6.0f

    private const val SPRINT_EXHAUSTION_PER_CM = 0.1f
    private const val SWIM_EXHAUSTION_PER_CM = 0.01f

    private const val SPRINT_JUMP_EXHAUSTION = 0.2f
    private const val JUMP_EXHAUSTION = 0.05f
    private const val MINE_EXHAUSTION = 0.005f

    @Volatile
    var saturation = 0.0f
        private set

    @Volatile
    var exhaustion = 0.0f
        private set

    private var foodLevel = 20
    private var tickTimer = 0
    //? if > 1.8.9
    private var lastPosition: Vec3? = null
    private var lastPosted = Float.NaN

    private val pendingExhaustion = ConcurrentLinkedQueue<Float>()

    fun initialize() {
        eventHandler { _: TickEvent.End -> tick() }

        //? if > 1.8.9 {
        eventHandler { (packet): PacketEvent.Receive ->
            if (packet !is ClientboundDamageEventPacket) return@eventHandler
            val player = mc.player ?: return@eventHandler
            val level = mc.level ?: return@eventHandler
            if (packet.entityId() != player.id) return@eventHandler
            pendingExhaustion.add(packet.getSource(level).foodExhaustion)
        }
        //?}
    }

    //~ if = 1.8.9 'FoodData' -> 'HungerManager'
    fun onServerSync(data: FoodData) {
        //~ if = 1.8.9 'foodData' -> 'hungerManager'
        if (mc.player?.foodData !== data) return
        foodLevel = data.foodLevel
        saturation = data.saturationLevel
        post()
    }

    fun onCauseFoodExhaustion(player: Player, amount: Float) {
        if (player !== mc.player) return
        addExhaustion(amount)
    }

    fun onJump(entity: LivingEntity) {
        //? if 1.21.1 || 1.8.9
        //if (true) return
        val player = mc.player ?: return
        if (entity !== player) return
        addExhaustion(if (player.isSprinting) SPRINT_JUMP_EXHAUSTION else JUMP_EXHAUSTION)
    }

    fun onDestroyBlock() {
        //~ if = 1.8.9 'mc.gameMode' -> 'mc.interactionManager'
        val gameMode = mc.gameMode ?: return
        //~ if = 1.8.9 'gameMode.playerMode' -> 'gameMode.gameMode'
        if (gameMode.playerMode.isCreative) return
        addExhaustion(MINE_EXHAUSTION)
    }

    private fun addExhaustion(amount: Float) {
        if (mc.player?.abilities?.invulnerable == true) return
        exhaustion = min(exhaustion + amount, MAX_EXHAUSTION)
    }

    private fun tick() {
        val player = mc.player
        if (player == null) {
            reset()
            return
        }

        while (true) {
            addExhaustion(pendingExhaustion.poll() ?: break)
        }

        //? if > 1.8.9
        trackMovement(player)
        simulateFoodTick(player)
        post()
    }

    private fun reset() {
        pendingExhaustion.clear()
        saturation = 0.0f
        exhaustion = 0.0f
        foodLevel = 20
        tickTimer = 0
        //? if > 1.8.9
        lastPosition = null
        lastPosted = Float.NaN
    }

    //? if > 1.8.9 {
    private fun trackMovement(player: Player) {
        val position = player.position()
        val previous = lastPosition
        lastPosition = position
        if (previous == null || player.isPassenger) return

        val dx = position.x - previous.x
        val dy = position.y - previous.y
        val dz = position.z - previous.z
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) return

        when {
            player.isSwimming || player.isEyeInFluid(FluidTags.WATER) -> {
                val cm = centimetres(dx, dy, dz)
                if (cm > 0) addExhaustion(SWIM_EXHAUSTION_PER_CM * cm * 0.01f)
            }

            player.isInWater -> {
                val cm = centimetres(dx, 0.0, dz)
                if (cm > 0) addExhaustion(SWIM_EXHAUSTION_PER_CM * cm * 0.01f)
            }

            player.onClimbable() -> {}

            player.onGround() -> {
                val cm = centimetres(dx, 0.0, dz)
                if (cm > 0 && player.isSprinting) addExhaustion(SPRINT_EXHAUSTION_PER_CM * cm * 0.01f)
            }
        }
    }

    private fun centimetres(dx: Double, dy: Double, dz: Double): Int =
        (sqrt(dx * dx + dy * dy + dz * dz).toFloat() * 100.0f).roundToInt()
    //?}

    private fun simulateFoodTick(player: Player) {
        if (exhaustion > EXHAUSTION_PER_SATURATION) {
            exhaustion -= EXHAUSTION_PER_SATURATION
            if (saturation > 0.0f) {
                saturation = max(saturation - 1.0f, 0.0f)
            //~ if = 1.8.9 'player.level()' -> 'player.world'
            } else if (player.level().difficulty != Difficulty.PEACEFUL) {
                foodLevel = max(foodLevel - 1, 0)
            }
        }

        //~ if = 1.8.9 'player.isHurt' -> 'player.needsHealing()'
        val hurt = player.isHurt

        //? if > 1.8.9 {
        if (saturation > 0.0f && hurt && foodLevel >= 20) {
            tickTimer++
            if (tickTimer >= FAST_REGEN_INTERVAL) {
                addExhaustion(min(saturation, FAST_REGEN_MAX_EXHAUSTION))
                tickTimer = 0
            }
        } else if (hurt && foodLevel >= 18) {
        //?} else
        //if (hurt && foodLevel >= 18) {
            tickTimer++
            if (tickTimer >= SLOW_REGEN_INTERVAL) {
                addExhaustion(SLOW_REGEN_EXHAUSTION)
                tickTimer = 0
            }
        } else if (foodLevel <= 0) {
            tickTimer++
            if (tickTimer >= SLOW_REGEN_INTERVAL) tickTimer = 0
        } else {
            tickTimer = 0
        }
    }

    private fun post() {
        if (saturation == lastPosted) return
        lastPosted = saturation
        EventManager.INSTANCE.post(SaturationChangedEvent(saturation))
    }
}
