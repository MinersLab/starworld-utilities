package starworld.core.server.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.item.ItemStack
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.text.HoverEvent.ItemStackContent
import net.minecraft.util.Formatting
import starworld.core.util.color
import starworld.core.util.hover
import starworld.core.util.text

object ShareItemCommand : CommandRegistrationCallback {

    fun toItemChatText(itemStack: ItemStack) = MutableText.of(TextContent.EMPTY).apply {
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
            CommandManager.literal("share-item").executes { context: CommandContext<ServerCommandSource> ->
                val player = context.source.playerOrThrow
                val playerManager = context.source.server.playerManager
                var itemStack = player.mainHandStack
                if (itemStack.isEmpty) itemStack = player.offHandStack
                val id = Registries.ITEM.getId(itemStack.item).toString()
                val text = MutableText.of(TextContent.EMPTY).apply {
                    append(toItemChatText(itemStack))
                    append(" ").append(
                        MutableText.of(TextContent.EMPTY)
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