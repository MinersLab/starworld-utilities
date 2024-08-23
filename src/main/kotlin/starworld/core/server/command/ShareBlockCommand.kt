package starworld.core.server.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.component.DataComponentTypes.CUSTOM_NAME
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import starworld.core.StarWorldCoreLib.rls
import starworld.core.util.color
import starworld.core.util.hover
import starworld.core.util.text
import java.util.stream.Stream

enum class ShareBlockCommand(val allowFluids: Boolean) : CommandRegistrationCallback {

    FLUID(true), BLOCK(false);

    override fun register(
        dispatcher: CommandDispatcher<ServerCommandSource>,
        registryAccess: CommandRegistryAccess,
        environment: CommandManager.RegistrationEnvironment
    ) {
        dispatcher.register(
            CommandManager.literal(rls(if (allowFluids) "share-fluid" else "share-block")).then(
                CommandManager.argument("position", BlockPosArgumentType.blockPos())
                    .executes {
                        handle(BlockPosArgumentType.getLoadedBlockPos(it, "position"), it)
                    }
            ).executes {
                val player = it.source.playerOrThrow
                val eyeBlock = player.raycast(15.0, 0f, allowFluids)
                if (eyeBlock is BlockHitResult) handle(eyeBlock.blockPos, it)
                else handle(player.blockPos, it)
            }
        )
    }

    fun handle(blockPos: BlockPos, context: CommandContext<ServerCommandSource>): Int {
        val playerManager = context.source.server.playerManager
        val world = context.source.playerOrThrow.serverWorld
        val blockState = world.getBlockState(blockPos)
        val block = blockState.block
        val blockEntity = world.getBlockEntity(blockPos)
        val itemStack = ItemStack(
            if (allowFluids) blockState.fluidState.let {
                if (it.isEmpty) Items.BUCKET
                else it.fluid.bucketItem
            }
            else block,
            1
        )
        if (allowFluids) itemStack.set(CUSTOM_NAME, text(FluidVariantAttributes.getName(FluidVariant.of(blockState.fluidState.fluid))).styled { it.withItalic(false) })
        blockEntity?.setStackNbt(
            itemStack,
            RegistryWrapper.WrapperLookup.of(Stream.of(Registries.BLOCK.readOnlyWrapper))
        )
        val id = if (allowFluids) Registries.FLUID.getId(blockState.fluidState.fluid).toString() else Registries.BLOCK.getId(block).toString()
        val text = Text.empty().apply {
            append(ShareItemCommand.toItemChatText(itemStack))
            append(" ")
                .append(
                    Text.empty()
                        .append("id ")
                        .append(text(id).hover(text(id)).color(Formatting.GREEN))
                        .styled { it.withColor(Formatting.GRAY) }
                )
            append(Text.empty().append(" at ").styled { it.withColor(Formatting.GRAY) })
            append(
                Text.empty().apply {
                    append(blockPos.toShortString())
                    styled {
                        it.withHoverEvent(
                            HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(blockPos.toShortString()))
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