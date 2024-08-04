package starworld.core

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import starworld.core.server.command.ShareBlockCommand
import starworld.core.server.command.ShareEntityCommand
import starworld.core.server.command.ShareItemCommand

object CommonProxy {

    val COMMANDS = listOf(ShareBlockCommand(true), ShareBlockCommand(false), ShareItemCommand, ShareEntityCommand)

    fun initializeCommands() {
        COMMANDS.forEach { CommandRegistrationCallback.EVENT.register(it) }
    }

    fun initialize() {
        initializeCommands()
    }

}