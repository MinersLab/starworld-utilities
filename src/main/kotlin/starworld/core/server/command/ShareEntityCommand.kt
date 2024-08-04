package starworld.core.server.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.Entity
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.util.Formatting
import starworld.core.util.color
import starworld.core.util.hover
import starworld.core.util.text

object ShareEntityCommand : CommandRegistrationCallback {

    fun toEntityChatText(entity: Entity) = MutableText.of(TextContent.EMPTY).apply {
        styled { style: Style ->
            style.withHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_ENTITY,
                    HoverEvent.EntityContent(entity.type, entity.uuid, entity.displayName)
                )
            )
        }
        append(entity.displayName)
    }

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.register(
            CommandManager.literal("share-entity").then(
                CommandManager.argument("selector", EntityArgumentType.entity())
                    .executes {
                        handle(EntityArgumentType.getEntity(it, "selector"), it)
                    }
            ).executes {
                handle(it.source.entityOrThrow, it)
            }
        )
    }

    fun handle(entity: Entity, context: CommandContext<ServerCommandSource>): Int {
        val playerManager = context.source.server.playerManager
        val text = MutableText.of(TextContent.EMPTY).apply {
            append(toEntityChatText(entity))
            append(" ")
                .append(
                    MutableText.of(TextContent.EMPTY)
                        .append("uuid ")
                        .append(text(entity.uuidAsString).hover(text(entity.uuidAsString)).color(Formatting.GREEN))
                        .append("")
                        .styled { it.withColor(Formatting.GRAY) }
                )
            append(MutableText.of(TextContent.EMPTY).append(" at ").styled { it.withColor(Formatting.GRAY) })
            append(
                MutableText.of(TextContent.EMPTY).apply {
                    append(entity.blockPos.toShortString())
                    styled {
                        it.withHoverEvent(
                            HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(entity.blockPos.toShortString()))
                        ).withColor(Formatting.GREEN)
                    }
                }
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
        return 0
    }

}