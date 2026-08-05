package org.polyfrost.evergreenhud.client.hooks

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
//? if > 1.8.9 {
import net.minecraft.network.chat.Component
//?} else
//import net.minecraft.client.resource.language.I18n
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.ItemStack
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.utils.v1.dsl.mc

const val ENDER_CHEST_SLOTS = 27

object EnderChestTracker {
    //? if > 1.8.9 {
    private val enderChestTitle = Component.translatable("container.enderchest").contents
    //?} else {
    /*private val enderChestTitle
        get() = I18n.translate("container.enderchest")
    *///?}

    private var contents: List<ItemStack> = emptyList()

    fun initialize() {
        eventHandler { _: TickEvent.End ->
            if (mc.player == null) {
                contents = emptyList()
            } else {
                capture()
            }
        }
    }

    fun contents(): List<ItemStack>? = contents.takeIf { it.isNotEmpty() }

    private fun capture() {
        //? if > 1.8.9 {
        //~ if < 26.2 'mc.gui.screen()' -> 'mc.screen'
        val screen = mc.gui.screen() as? AbstractContainerScreen<*> ?: return
        //?} else
        //val screen = mc.screen as? InventoryMenuScreen ?: return
        val menu = screen.menu as? ChestMenu ?: return
        //? if > 1.8.9 {
        if (menu.rowCount != 3 || screen.title.contents != enderChestTitle) return
        //?} else
        //if (menu.chest.size / 9 != 3 || menu.chest.displayName.string != enderChestTitle) return

        //~ if = 1.8.9 'menu.container' -> 'menu.chest'
        val container = menu.container
        //~ if = 1.8.9 'container.containerSize' -> 'container.size'
        if (container.containerSize != ENDER_CHEST_SLOTS) return
        contents = List(ENDER_CHEST_SLOTS) { container.getItem(it).copy() }
    }
}
