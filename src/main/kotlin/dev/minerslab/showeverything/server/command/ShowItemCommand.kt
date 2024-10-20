package dev.minerslab.showeverything.server.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.minerslab.showeverything.util.color
import dev.minerslab.showeverything.util.hover
import dev.minerslab.showeverything.util.text
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.item.ItemStack
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.HoverEvent
import net.minecraft.text.HoverEvent.ItemStackContent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object ShowItemCommand : CommandRegistrationCallback {

    fun toItemChatText(itemStack: ItemStack) = Text.empty().apply {
        if (itemStack.count > 1) append("${itemStack.count} * ")
        styled { style: Style ->
            style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_ITEM,
                    ItemStackContent(itemStack)
                )
            )
        }
        append(itemStack.name)
    }

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.register(
            CommandManager.literal("show-item").executes { context: CommandContext<ServerCommandSource> ->
                val player = context.source.playerOrThrow
                val playerManager = context.source.server.playerManager
                var itemStack = player.mainHandStack
                if (itemStack.isEmpty) itemStack = player.offHandStack
                val id = Registries.ITEM.getId(itemStack.item).toString()
                val text = Text.empty().apply {
                    append(toItemChatText(itemStack))
                    append(" ").append(
                        Text.empty()
                            .append("id ")
                            .append(text(id).hover(text(id)).color(Formatting.GREEN))
                            .styled { it.withColor(Formatting.GRAY) }
                    )
                }
                val message = SignedMessage.ofUnsigned("").withUnsignedContent(text)
                playerManager.broadcast(
                    message,
                    context.source,
                    MessageType.params(
                        MessageType.CHAT,
                        context.source
                    )
                )
                0
            }
        )
    }

}