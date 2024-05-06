package me.ddayo.customscript.client.gui.script

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import me.ddayo.customscript.client.gui.GuiBase
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.blocks.*
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.options.Option
import net.minecraft.client.Minecraft
import java.util.*

interface StackableScript {
    val avScripts: MutableList<CSExecutor>

    fun iterateScript(f: CSExecutor.() -> Unit) { avScripts.reversed().forEach { f(it) } }

    fun pushScreen(screen: CSExecutor) { avScripts.add(screen) }

    fun popScreen() { avScripts.removeLast() }

    fun onFinish() { iterateScript { finish() } }

    fun onTick() { iterateScript { tick() } }

    fun onKeyPressed(keyCode: Int, scanCode: Int, modifiers: Int) { iterateScript { keyPressed(keyCode, scanCode, modifiers) } }

    fun onKeyReleased(keyCode: Int, scanCode: Int, modifiers: Int) { iterateScript { keyReleased(keyCode, scanCode, modifiers) } }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int) { iterateScript { mouseClicked(mouseX, mouseY, button) } }

    fun RenderUtil.tryRender() {
        avScripts.removeIf { !it.isValid }

        iterateScript {
            CSExecutor.RenderParse.values().forEach { ph ->
                renderable[ph]!!.forEach { f -> if (!f.isLoading) f.render(this@tryRender) }
            }
        }
    }
}

class StackableScriptScreen(mode: ScriptMode) : GuiBase(), StackableScript {
    private val renderInit by lazy {
        if (mode == ScriptMode.Gui)
            Minecraft.getInstance().mouseHelper.ungrabMouse()
    }

    override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderInit
        super.render(matrixStack, mouseX, mouseY, partialTicks)

        Minecraft.getInstance().mainWindow.let { w ->
            CalculableValueManager.setValueQuiet(
                "mouseX",
                (mouseX - (w.scaledWidth - w.scaledHeight * 16 / 9) / 2) * 1080 / w.scaledHeight
            )
            CalculableValueManager.setValueQuiet(
                "mouseY",
                mouseY * 1080 / w.scaledHeight
            )
            CalculableValueManager.setValueQuiet("partialTick", partialTicks)
        }
        CalculableValueManager.frame()

        RenderSystem.enableBlend()
        RenderUtil.renderer.loadMatrix(matrixStack) {
            FHDScale {
                tryRender()
            }
        }
        RenderSystem.disableBlend()
    }

    override fun onClose() {
        super.onClose()
        onFinish()
    }

    override fun tick() {
        super.tick()
        onTick()
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        onKeyReleased(keyCode, scanCode, modifiers)
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        onKeyPressed(keyCode, scanCode, modifiers)
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int) =
        mouseHandler(mouseX, mouseY) { mx, my ->
            onMouseClicked(mx, my, button)
            super.mouseReleased(mouseX, mouseY, button)
        }

    override val avScripts: MutableList<CSExecutor> = mutableListOf()
}

enum class ScriptMode {
    Gui, Hud
}