package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.network.ServerSideCommandNetworkHandler
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft


object RunCommandBlockInitializer: BlockInitializer {
    override val name = "RunCommandBlock"
    override fun initialize(context: Option) = RunCommandBlock(context)
}

class RunCommandBlock(context: Option): BlockBase(context), ICalculableHolder {
    private val command = StringCalculable(context["Command"].string!!)
    private val side = when(context["Side"].string) {
        "Client" -> 0
        "Server" -> 1
        else -> throw CompileError("Not supported value on RunCommandBlock")
    }

    override fun onEnter(base: CSExecutor) {
        if(side == 0) Minecraft.getInstance().player?.sendChatMessage("/" + command.get)
        else CustomScript.network.sendToServer(ServerSideCommandNetworkHandler(command.get))
        super.onEnter(base)
    }

    override val calculable by lazy { listOf(command) }
}