package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor

abstract class ScriptRenderer {
    abstract fun RenderUtil.renderInternal()
    open fun onRemovedFromQueue() {}
    abstract val renderParse: CSExecutor.RenderParse

    fun render(renderer: RenderUtil) {
        renderer.renderInternal()
    }

    abstract val isLoading: Boolean
}

