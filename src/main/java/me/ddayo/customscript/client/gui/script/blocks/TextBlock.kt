package me.ddayo.customscript.client.gui.script.blocks

import com.mojang.blaze3d.matrix.MatrixStack
import me.ddayo.customscript.client.gui.FontResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.font.FontedText
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.DoubleCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft


object TextBlockInitializer: BlockInitializer {
    override val name = "TextBlock"

    override fun initialize(context: Option) = TextBlock(context)
}

class TextBlock(context: Option) : BlockBase(context) {
    private class TextScriptRenderer(
        private val text: StringCalculable,
        private val textX: DoubleCalculable,
        private val textY: DoubleCalculable,
        private val textScale: DoubleCalculable,
        private val textFont: String,
        private val textColor: UInt
    ) : ScriptRenderer(), ICalculableHolder {
        private val usingCustomFont = textFont.isNotBlank()
        lateinit var fontedText: FontedText

        init {
            if (usingCustomFont) {
                FontResource.getOrCreate(textFont).run {
                    fontedText = FontedText(this, text.get).setHeight(textScale.get.toInt())
                }
                text.setUpdateHook {
                    fontedText.updateText(it)
                }
            }
        }

        override val renderParse: CSExecutor.RenderParse
            get() = CSExecutor.RenderParse.Post

        override fun onRemovedFromQueue() {
            if (usingCustomFont)
                fontedText.free()
        }

        override fun RenderUtil.renderInternal() {
            push {
                if (!usingCustomFont) {
                    scale(textScale.get, textScale.get, textScale.get)
                    Minecraft.getInstance().fontRenderer.drawString(
                        MatrixStack(),
                        text.get,
                        (textX.get / textScale.get).toFloat(),
                        (textY.get / textScale.get).toFloat(),
                        textColor.toInt()
                    )
                } else fontedText.render(textX.get, textY.get, textColor)
            }
        }

        override val calculable by lazy { listOf(text, textX, textY, textScale) }
        override val isLoading: Boolean
            get() = usingCustomFont && !FontResource.getOrCreate(textFont).isLoaded
    }

    private val text = StringCalculable(context["Text"].string!!)
    private val textX = DoubleCalculable(context["TextX"].string!!)
    private val textY = DoubleCalculable(context["TextY"].string!!)
    private val textScale = DoubleCalculable(context["TextScale"].string ?: "1.0")
    private val textFont = context["TextFont"].string ?: ""
    private val textColor = (context["TextColor"].string ?: "ffffff").toUInt(16)


    override val rendererInstance: ScriptRenderer
        get() = TextScriptRenderer(text, textX, textY, textScale, textFont, textColor)
}