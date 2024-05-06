package me.ddayo.customscript.network

import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.gui.script.CSExecutor
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import org.apache.logging.log4j.LogManager
import java.util.function.Supplier

class OpenScriptNetworkHandler() {
    var script = ""
    var begin = ""

    constructor(script: String, begin: String): this() {
        this.script = script
        this.begin = begin
    }

    constructor(script: String): this() {
        this.script = script
        this.begin = "default"
    }

    companion object {
        fun onMessageReceived(message: OpenScriptNetworkHandler, ctxSuf: Supplier<NetworkEvent.Context>) = message.run {
            val ctx = ctxSuf.get()
            ctx.packetHandled = true
            ctx.enqueueWork {
                val sc = CSExecutor.fromFile(script, begin, true)
                if(sc == null) {
                    LogManager.getLogger().info("Cannot load script: $script")
                    return@enqueueWork
                }
                ClientDataHandler.addScreen(sc)
            }
        }

        @JvmStatic
        fun decode(buf: PacketBuffer) = OpenScriptNetworkHandler(buf.readString(), buf.readString())
    }

    fun encode(buf: PacketBuffer)
            = buf.writeString(script)
        .writeString(begin)
}