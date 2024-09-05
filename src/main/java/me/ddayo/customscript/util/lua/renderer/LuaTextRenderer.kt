package me.ddayo.customscript.util.lua.renderer

import me.ddayo.customscript.client.gui.FontResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.font.FontedText
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.client.gui.script.blocks.ScriptRenderer
import net.minecraft.client.Minecraft


typealias DC = () -> Double
typealias SC = () -> String
typealias IC = () -> Number

class LuaTextRenderer(
    private val text: SC,
    private val textX: DC,
    private val textY: DC,
    private val textScale: DC,
    private val textFont: String,
    private val textColor: IC,
    override val renderParse: CSExecutor.RenderParse
) : ScriptRenderer() {
    private val usingCustomFont = textFont.isNotBlank()
    private lateinit var fontedText: FontedText

    private var currentText = text()
    private var currentScale = textScale()

    init {
        if (usingCustomFont) {
            FontResource.getOrCreate(textFont).run {
                fontedText = FontedText(this, currentText).setHeight(currentScale.toInt())
            }
        }
    }

    override fun onRemovedFromQueue() {
        if (usingCustomFont)
            fontedText.free()
    }

    override fun RenderUtil.renderInternal() {
        push {
            if (!usingCustomFont) {
                scale(textScale(), textScale(), textScale())
                Minecraft.getInstance().fontRenderer.drawString(
                    matrix,
                    text(),
                    (textX() / textScale()).toFloat(),
                    (textY() / textScale()).toFloat(),
                    textColor().toInt()
                )
            } else {
                val cf = text()
                val txl = textScale()
                if(cf != currentText || txl != currentScale) {
                    fontedText.free()
                    currentText = cf
                    currentScale = txl
                    FontResource.getOrCreate(textFont).run {
                        fontedText = FontedText(this, currentText).setHeight(txl.toInt())
                    }
                }
                fontedText.render(textX(), textY(), textColor().toLong().toUInt())
            }
        }
    }

    override val isLoading: Boolean
        get() = usingCustomFont && !FontResource.getOrCreate(textFont).isLoaded
}