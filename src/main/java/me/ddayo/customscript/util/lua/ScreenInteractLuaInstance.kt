package me.ddayo.customscript.util.lua

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.gui.GuiBase
import me.ddayo.customscript.client.gui.RenderUtil
import party.iroiro.luajava.AbstractLua


class ScriptedScreen(private val script: ScreenScriptableInstance): GuiBase() {
    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(matrixStack, mouseX, mouseY, partialTicks)
        script.setMousePos(mouseX.toDouble(), mouseX.toDouble())
        RenderUtil.renderer.loadMatrix(matrixStack) {
            FHDScale {
                script.renderer.render(this)
            }
        }
    }

    override fun tick() {
        script.invokeTick()
        if(script.finished) closeScreen()
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) = mouseHandler(mouseX, mouseY) { mx, my ->
        script.invokeOnMouseRelease(mx, my, button)
        if(script.finished) closeScreen()
        true
    }
}

open class ScreenInteractLuaEngine(coreScriptDirStr: String): RenderableLuaEngine(coreScriptDirStr) {
    init {
        loadCoreExtensionWithName("csx", "screen_interact/csx")
    }

    override fun newScriptHandler(name: String, script: String, newLua: AbstractLua): ILuaScriptHandler {
        return ScreenScriptableInstance(name, script, newLua)
    }
}

open class ScreenScriptableInstance(name: String, script: String, lua: AbstractLua): RenderableScriptInstance(name, script, lua) {
    fun setMousePos(mx: Double, my: Double) {
        lua.push(mx)
        lua.setGlobal("mouse_x")
        lua.push(my)
        lua.setGlobal("mouse_y")
    }

    val renderer by lazy { ev["csx"]["renderer"].toJavaObject() as RenderableLuaEngine.LuaRenderer }

    fun invokeOnMouseRelease(mx: Double, my: Double, button: Int) = invokeEvent("on_mouse_release") {
        lua.getGlobal("cmath")
        lua.getField(-1, "point")
        lua.getField(-1, "new")
        // push cmath.point
        lua.pushValue(-2)
        // first argument
        lua.push(mx)
        // second argument
        lua.push(my)
        // last argument
        lua.pushNil()
        // because `self` exists, call with 4 args
        orError { lua.pCall(4, 1) }
        // remove `point`, [-1] is the point value to pass.
        lua.remove(-2)
        // remove 'cmath`
        lua.remove(-2)
        // pass mouse button to event
        lua.push(button)
        2
    }
}