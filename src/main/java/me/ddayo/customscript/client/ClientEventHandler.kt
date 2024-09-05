package me.ddayo.customscript.client

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.lua.BaseLuaEngine
import me.ddayo.customscript.util.lua.ScreenInteractLuaEngine
import me.ddayo.customscript.util.lua.ScreenScriptableInstance
import me.ddayo.customscript.util.lua.ScriptedScreen
import net.minecraft.client.gui.screen.MainMenuScreen
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.io.File

object ClientEventHandler {
    @SubscribeEvent
    public fun onRenderHud(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return
        RenderUtil.renderer.loadMatrix(event.matrixStack) {
            FHDScale(event.window.scaledWidth, event.window.scaledHeight) {
                ClientDataHandler.enabledHud.forEach {
                    CSExecutor.RenderParse.values().forEach { ph ->
                        it.value.renderable[ph]!!.forEach {
                            it.render(RenderUtil.renderer)
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public fun mainSc(event: GuiOpenEvent) {
        if(event.gui is MainMenuScreen)
            event.gui = ScriptedScreen(ScreenInteractLuaEngine(BaseLuaEngine.CORE_DIR).runScript("name", File(BaseLuaEngine.CORE_DIR, "example.lua").readText()) as ScreenScriptableInstance)
    }


    @SubscribeEvent
    public fun tick(event: TickEvent.ClientTickEvent) {
        CalculableValueManager.tick()
    }
}