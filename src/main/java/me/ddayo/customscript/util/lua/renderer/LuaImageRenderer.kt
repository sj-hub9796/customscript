package me.ddayo.customscript.util.lua.renderer

import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.client.gui.script.blocks.ScriptRenderer


// TODO
class LuaImageRenderer (
    private val image: SC,
    private val biasX: DC,
    private val biasY: DC,
    private val width: DC,
    private val height: DC,
    private val customPos: Boolean,
    private val autoSize: Boolean,
    private val parse: String
) : ScriptRenderer() {
    override fun RenderUtil.renderInternal() {
        image().split("\n").forEach {
            if (it.isNotBlank())
                useTexture(ImageResource.getOrCreate(it)) {
                    if(!customPos)
                        render()
                    else if (autoSize)
                        render(biasX().toInt(), biasY().toInt(), getTexWidth(), getTexHeight())
                    else render(biasX(), biasY(), width(), height())
                }
        }
    }

    override val renderParse: CSExecutor.RenderParse
        get() = CSExecutor.RenderParse.valueOf(parse)

    override val isLoading: Boolean
        get() = image().split("\n").any { !ImageResource.getOrCreate(it).isLoaded }
}
