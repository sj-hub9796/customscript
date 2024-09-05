package me.ddayo.customscript.util.lua.renderer

import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.client.gui.script.blocks.ScriptRenderer


class LuaImageRenderer (
    private val image: SC,
    private val biasX: () -> Double?,
    private val biasY: () -> Double?,
    private val width: () -> Double?,
    private val height: () -> Double?,
    private val parse: String
) : ScriptRenderer() {
    override fun RenderUtil.renderInternal() {
        image().split("\n").forEach {
            if (it.isNotBlank())
                useTexture(ImageResource.getOrCreate(it)) {
                    render(biasX()?.toInt() ?: 0, biasY()?.toInt() ?: 0, width()?.toInt() ?: getTexWidth(), height()?.toInt() ?: getTexHeight())
                }
        }
    }

    override val renderParse: CSExecutor.RenderParse
        get() = CSExecutor.RenderParse.valueOf(parse)

    override val isLoading: Boolean
        get() = image().split("\n").any { !ImageResource.getOrCreate(it).isLoaded }
}
