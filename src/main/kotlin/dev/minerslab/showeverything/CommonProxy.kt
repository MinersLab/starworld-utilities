package dev.minerslab.showeverything

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import dev.minerslab.showeverything.server.command.ShowBlockCommand
import dev.minerslab.showeverything.server.command.ShowEntityCommand
import dev.minerslab.showeverything.server.command.ShowItemCommand

object CommonProxy {

    val COMMANDS = listOf(
        *ShowBlockCommand.entries.toTypedArray(),
        ShowItemCommand,
        ShowEntityCommand
    )

    fun initializeCommands() {
        COMMANDS.forEach { CommandRegistrationCallback.EVENT.register(it) }
    }

    fun initialize() {
        initializeCommands()
    }

}