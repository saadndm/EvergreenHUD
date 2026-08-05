plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "template"

    replacements {
        string(current.parsed < "1.21.11") {
            replace("net.minecraft.Util", "net.minecraft.util.Util")
        }

        string(eval(current.version, "= 1.8.9")) {
            replace(
                "net.minecraft.client.player.LocalPlayer",
                "net.minecraft.client.entity.living.player.LocalClientPlayerEntity"
            )
            replace(
                "net.minecraft.client.gui.screens.inventory.InventoryScreen",
                "net.minecraft.client.gui.screen.inventory.menu.SurvivalInventoryScreen"
            )
            replace(
                "net.minecraft.client.renderer.entity.LivingEntityRenderer",
                "net.minecraft.client.render.entity.LivingEntityRenderer"
            )
            replace(
                "net.minecraft.world.entity.Entity",
                "net.minecraft.entity.Entity"
            )
            replace(
                "net.minecraft.world.entity.player.Player",
                "net.minecraft.entity.living.player.PlayerEntity"
            )
            replace(
                "net.minecraft.world.entity.player.Inventory",
                "net.minecraft.entity.living.player.PlayerInventory"
            )
            replace(
                "net.minecraft.server.Bootstrap",
                "net.minecraft.Bootstrap"
            )
            replace(
                "net.minecraft.world.entity.LivingEntity",
                "net.minecraft.entity.living.LivingEntity"
            )
            replace(
                "net.minecraft.world.food.FoodData",
                "net.minecraft.entity.living.player.HungerManager"
            )
            replace(
                "net.minecraft.world.item.BlockItem",
                "net.minecraft.item.BlockItem"
            )
            replace(
                "net.minecraft.world.item.ItemStack",
                "net.minecraft.item.ItemStack"
            )
            replace(
                "net.minecraft.world.level.Level",
                "net.minecraft.world.World"
            )
            replace(
                "net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket",
                "net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket"
            )
            replace(
                "net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket",
                "net.minecraft.network.packet.s2c.play.BlocksUpdateS2CPacket"
            )
            replace(
                "net.minecraft.core.BlockPos",
                "net.minecraft.util.math.BlockPos"
            )
            replace(
                "net.minecraft.world.level.block.state.BlockState",
                "net.minecraft.block.state.BlockState"
            )
            replace(
                "net.minecraft.world.inventory.ChestMenu",
                "net.minecraft.inventory.menu.ChestMenu"
            )
            replace(
                "net.minecraft.client.gui.screens.inventory.AbstractContainerScreen",
                "net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen"
            )
            replace(
                "net.minecraft.world.item.Items",
                "net.minecraft.item.Items"
            )
        }
    }
}

stonecutter tasks {
    order("publishModrinth")
}
