package me.ddayo.customscript

import me.ddayo.customscript.CustomScript.MOD_ID
import me.ddayo.customscript.client.ClientEventHandler
import me.ddayo.customscript.client.gui.Resource
import me.ddayo.customscript.network.*
import me.ddayo.customscript.server.ServerEventHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.ReloadListener
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.resources.SimpleReloadableResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.simple.SimpleChannel
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.*

@Mod(MOD_ID)
class CustomScriptMod {
    init {
        FMLJavaModLoadingContext.get().modEventBus.register(this)
    }

    @SubscribeEvent
    fun serverInit(event: FMLDedicatedServerSetupEvent) {
        MinecraftForge.EVENT_BUS.register(ServerEventHandler)
    }

    @SubscribeEvent
    fun clientInit(event: FMLClientSetupEvent) {
        CustomScript.isClient = true
        MinecraftForge.EVENT_BUS.register(ClientEventHandler)
        // MinecraftForge.EVENT_BUS.register(ServerEventHandler)
        File("customscript").mkdir()

        event.enqueueWork {
            Minecraft.getInstance().apply {
                (resourceManager as SimpleReloadableResourceManager).addReloadListener(object :
                    ReloadListener<Unit>() {
                    override fun prepare(resourceManagerIn: IResourceManager, profilerIn: IProfiler) {}

                    override fun apply(objectIn: Unit, resourceManagerIn: IResourceManager, profilerIn: IProfiler) {
                        Resource.clearResource()
                        LogManager.getLogger().info("Custom script resource has been cleared via minecraft")
                    }
                })
            }
        }
    }

    @SubscribeEvent
    @Suppress("INACCESSIBLE_TYPE")
    fun init(event: FMLCommonSetupEvent) {
        CustomScript.isTest = false

        CustomScript.network = NetworkRegistry.newSimpleChannel(
                ResourceLocation(MOD_ID, "networkchannel"),
                { CustomScript.VERSION },
                CustomScript.VERSION::equals,
                CustomScript.VERSION::equals
        )
        CustomScript.network.registerMessage(
                11, OpenScriptNetworkHandler::class.java,
                OpenScriptNetworkHandler::encode, OpenScriptNetworkHandler.Companion::decode,
                OpenScriptNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
        CustomScript.network.registerMessage(
                12, ServerSideCommandNetworkHandler::class.java,
                ServerSideCommandNetworkHandler::encode, ServerSideCommandNetworkHandler.Companion::decode,
                ServerSideCommandNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER)
        )
        CustomScript.network.registerMessage(
                13, ClearCacheNetworkHandler::class.java,
                ClearCacheNetworkHandler::encode, ClearCacheNetworkHandler.Companion::decode,
                ClearCacheNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
        CustomScript.network.registerMessage(
                14, QueueCloseGuiNetworkHandler::class.java,
                QueueCloseGuiNetworkHandler::encode, QueueCloseGuiNetworkHandler.Companion::decode,
                QueueCloseGuiNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
        CustomScript.network.registerMessage(
                15, CloseGuiNetworkHandler::class.java,
                CloseGuiNetworkHandler::encode, CloseGuiNetworkHandler.Companion::decode,
                CloseGuiNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_SERVER)
        )
        CustomScript.network.registerMessage(
                16, UpdateValueNetworkHandler::class.java,
                UpdateValueNetworkHandler::encode, UpdateValueNetworkHandler.Companion::decode,
                UpdateValueNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
        CustomScript.network.registerMessage(
            17, HudNetworkHandler::class.java,
            HudNetworkHandler::encode, HudNetworkHandler.Companion::decode,
            HudNetworkHandler.Companion::onMessageReceived, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        )
    }
}

object CustomScript {
    const val MOD_ID = "customscript"
    const val VERSION = "0.7"

    var isClient = false
        internal set

    var isTest = true

    lateinit var network: SimpleChannel
}