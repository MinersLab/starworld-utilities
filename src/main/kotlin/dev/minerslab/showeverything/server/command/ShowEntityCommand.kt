package dev.minerslab.showeverything.server.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.minerslab.showeverything.util.color
import dev.minerslab.showeverything.util.hover
import dev.minerslab.showeverything.util.text
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.Entity
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.hit.EntityHitResult

object ShowEntityCommand : CommandRegistrationCallback {

    fun toEntityChatText(entity: Entity) = Text.empty().apply {
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
            CommandManager.literal("show-entity").then(
                CommandManager.argument("selector", EntityArgumentType.entity())
                    .executes {
                        handle(EntityArgumentType.getEntity(it, "selector"), it)
                    }
            ).executes {
                val raycast = it.source.playerOrThrow.raycast(15.0, 0f, false)
                handle(if (raycast is EntityHitResult) raycast.entity else it.source.entityOrThrow, it)
            }
        )
    }

    fun handle(entity: Entity, context: CommandContext<ServerCommandSource>): Int {
        val playerManager = context.source.server.playerManager
        val text = Text.empty().apply {
            append(toEntityChatText(entity))
            append(" ")
                .append(
                    Text.empty()
                        .append("uuid ")
                        .append(text(entity.uuidAsString).hover(text(entity.uuidAsString)).color(Formatting.GREEN))
                        .append("")
                        .styled { it.withColor(Formatting.GRAY) }
                )
            append(Text.empty().append(" at ").styled { it.withColor(Formatting.GRAY) })
            append(
                Text.empty().apply {
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