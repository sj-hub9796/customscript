package me.ddayo.customscript.server.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.ClientDataHandler
import me.ddayo.customscript.client.gui.FontResource
import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.network.ClearCacheNetworkHandler
import me.ddayo.customscript.network.ClearCacheNetworkHandler.CacheType
import me.ddayo.customscript.server.ServerConfiguration
import me.ddayo.customscript.server.ServerDataHandler
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.GameProfileArgument
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.fml.network.NetworkDirection
import net.minecraftforge.fml.server.ServerLifecycleHooks
import org.apache.logging.log4j.LogManager

object CustomScriptCommand: CommandHandler.ICommand {
    public val baseCommand = Commands.literal("cs")
        .then(Commands.literal("open")
            .requires {
                it.hasPermissionLevel(2)
            }
            .then(
                Commands.argument("script", StringArgumentType.string())
                    .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .then(Commands.argument("begin position", StringArgumentType.string())
                            .executes { execute(it) })
                        .executes { execute(it) })
            )
        )
        .then(
            Commands.literal("hud")
                .requires { it.hasPermissionLevel(2) }
                .then(
                    Commands.literal("enable")
                        .then(
                            Commands.argument("script", StringArgumentType.string())
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                    .executes {
                                        val script = it.getArgument("script", String::class.java)
                                        val players = try {
                                            GameProfileArgument.getGameProfiles(it, "player")
                                                .map { profile -> ServerLifecycleHooks.getCurrentServer().playerList.players.first { it.uniqueID == profile.id } }
                                        } catch (_: IllegalArgumentException) {
                                            listOf(it.source.asPlayer())
                                        }
                                        ServerDataHandler.enableHud(script, *players.toTypedArray())
                                        1
                                    })
                        )
                )
                .then(
                    Commands.literal("disable")
                        .then(
                            Commands.argument("script", StringArgumentType.string())
                                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                    .executes {
                                        val script = it.getArgument("script", String::class.java)
                                        val players = try {
                                            GameProfileArgument.getGameProfiles(it, "player")
                                                .map { profile -> ServerLifecycleHooks.getCurrentServer().playerList.players.first { it.uniqueID == profile.id } }
                                        } catch (_: IllegalArgumentException) {
                                            listOf(it.source.asPlayer())
                                        }
                                        ServerDataHandler.disableHud(script, *players.toTypedArray())
                                        1
                                    })
                        )
                )
        )
        .then(
            Commands.literal("cache")
                .then(
                    Commands.literal("image")
                        .then(Commands.literal("clear")
                            .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes {
                                    GameProfileArgument.getGameProfiles(it, "player")
                                        .map { ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(it.id) }
                                        .forEach {
                                            CustomScript.network.sendTo(
                                                ClearCacheNetworkHandler(CacheType.IMAGE),
                                                it!!.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                                            )
                                        }
                                    1
                                })
                            .executes {
                                if (CustomScript.isClient)
                                    ImageResource.clearResource()
                                else CustomScript.network.sendTo(
                                    ClearCacheNetworkHandler(CacheType.IMAGE),
                                    it.source.asPlayer().connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                                )
                                1
                            })
                )
                .then(
                    Commands.literal("font")
                        .then(Commands.literal("clear")
                            .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes {
                                    GameProfileArgument.getGameProfiles(it, "player")
                                        .map { ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(it.id) }
                                        .forEach {
                                            CustomScript.network.sendTo(
                                                ClearCacheNetworkHandler(CacheType.FONT),
                                                it!!.connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                                            )
                                        }
                                    1
                                })
                            .executes {
                                if (CustomScript.isClient)
                                    FontResource.clearResource()
                                else CustomScript.network.sendTo(
                                    ClearCacheNetworkHandler(CacheType.FONT),
                                    it.source.asPlayer().connection.networkManager, NetworkDirection.PLAY_TO_CLIENT
                                )
                                1
                            })
                )
        )
        .then(Commands.literal("value")
            .requires { it.hasPermissionLevel(2) }
            .then(
                Commands.argument("player", GameProfileArgument.gameProfile())
                    .then(
                        Commands.argument("value name", StringArgumentType.string())
                            .then(Commands.argument("value", StringArgumentType.string())
                                .executes {
                                    GameProfileArgument.getGameProfiles(it, "player").map { profile ->
                                        ServerLifecycleHooks.getCurrentServer().playerList.getPlayerByUUID(
                                            profile.id
                                        )!!
                                    }.forEach { p ->
                                        ServerDataHandler.updateData(
                                            it.source,
                                            p,
                                            it.getArgument("value name", String::class.java),
                                            it.getArgument("value", String::class.java)
                                        )
                                    }
                                    1
                                })
                    )
            )
        )
        .then(Commands.literal("internal-value")
            .requires { it.hasPermissionLevel(2) }
            .then(
                Commands.argument("value name", StringArgumentType.string())
                    .then(Commands.argument("value", StringArgumentType.string())
                        .executes {
                            ServerDataHandler.updateInternalData(
                                it.source,
                                it.getArgument("value name", String::class.java),
                                it.getArgument("value", String::class.java)
                            )
                            1
                        })
            )
        )
        .then(
            Commands.literal("show-value")
                .requires { it.hasPermissionLevel(2) }
                .then(
                    Commands.argument("player", GameProfileArgument.gameProfile())
                        .then(Commands.argument("value name", StringArgumentType.string())
                            .executes {
                                it.getArgument("value name", String::class.java).run {
                                    it.source.sendFeedback(
                                        StringTextComponent(
                                            "Value $this: ${
                                                ServerDataHandler.dynamicData[GameProfileArgument.getGameProfiles(it, "player").first().id]?.get(this)
                                            }"
                                        ),
                                        true
                                    )
                                }
                                1
                            })
                )
        )
        .then(
            Commands.literal("show-internal-value")
                .then(Commands.argument("value name", StringArgumentType.string())
                    .executes {
                        it.getArgument("value name", String::class.java).run {
                            it.source.sendFeedback(
                                StringTextComponent("Internal value $this: ${ServerDataHandler.internalData[this]}"),
                                true
                            )
                        }
                        1
                    })
        )
        .then(Commands.literal("reload-config")
            .executes {
                ServerConfiguration.loadConfig()
                1
            })


    override fun register(dispatcher: CommandDispatcher<CommandSource>) {
        dispatcher.register(baseCommand)
    }

    private fun execute(it: CommandContext<CommandSource>): Int {
        try {
            val script = it.getArgument("script", String::class.java)
            val players = try {
                GameProfileArgument.getGameProfiles(it, "player")
                    .map { profile -> ServerLifecycleHooks.getCurrentServer().playerList.players.first { it.uniqueID == profile.id } }
            } catch (_: IllegalArgumentException) {
                listOf(it.source.asPlayer())
            }
            val begin = try {
                it.getArgument("begin position", String::class.java)
            } catch (_: IllegalArgumentException) {
                "default"
            }

            if (CustomScript.isClient)
                ClientDataHandler.addScreen(CSExecutor.fromFile(script, begin, true)!!)
            else ServerDataHandler.openScriptOnPlayers(script, begin, *players.toTypedArray())
        } catch(e: Exception) {
            LogManager.getLogger().error(e.message)
        }
        return 1
    }
}