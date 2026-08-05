@file:JvmName("SmuggledHudDrawContext")

package org.polyfrost.evergreenhud.client.hooks

//? if > 1.8.9 {
//? if < 26
//import net.minecraft.client.gui.GuiGraphics
//? if >= 26
import net.minecraft.client.gui.GuiGraphicsExtractor as GuiGraphics

var smuggledHudDrawContext: GuiGraphics? = null
//?}

var smuggledHudPartialTick = 1f
