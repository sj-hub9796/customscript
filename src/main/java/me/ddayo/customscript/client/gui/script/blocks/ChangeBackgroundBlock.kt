package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.DoubleCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.bool
import me.ddayo.customscript.util.options.Option.Companion.string
import java.util.*


object ChangeBackgroundBlockInitializer: BlockInitializer {
    override val name = "ChangeBackgroundBlock"
    override fun initialize(context: Option) = ChangeBackgroundBlock(context)
}

class ChangeBackgroundBlock(context: Option) : BlockBase(context) {
    private class BackgroundRenderer (
        private val image: StringCalculable,
        private val biasX: DoubleCalculable?,
        private val biasY: DoubleCalculable?,
        private val width: DoubleCalculable?,
        private val height: DoubleCalculable?,
        private val customPos: Boolean,
        private val autoSize: Boolean,
        private val parse: String
    ) : ScriptRenderer(), ICalculableHolder {
        override fun RenderUtil.renderInternal() {
            image.get.split("\n").forEach {
                if (it.isNotBlank())
                    useTexture(ImageResource.getOrCreate(it)) {
                        if(!customPos)
                            render()
                        else if (autoSize)
                            render(biasX!!.get.toInt(), biasY!!.get.toInt(), getTexWidth(), getTexHeight())
                        else render(biasX!!.get, biasY!!.get, width!!.get, height!!.get)
                    }
            }
        }

        override val renderParse: CSExecutor.RenderParse
            get() = CSExecutor.RenderParse.valueOf(parse)
        override val calculable = listOfNotNull(image, biasX, biasY, width, height)
        override val isLoading: Boolean
            get() = image.get.split("\n").any { !ImageResource.getOrCreate(it).isLoaded }
    }

    private val type = when (context["Type"].string) {
        "Push" -> 0
        "Reset" -> 1
        "Pop" -> 2
        else -> throw CompileError("Not supported value on ChangeBackgroundBlock")
    }
    private val image = StringCalculable(context["Images"].string!!)
    private val customPos = context["CustomPos"].bool == true
    private val biasX: DoubleCalculable?
    private val biasY: DoubleCalculable?
    private val autoSize: Boolean
    private val width: DoubleCalculable?
    private val height: DoubleCalculable?

    init {
        if (customPos) {
            biasX = DoubleCalculable(context["BiasX"].string!!)
            biasY = DoubleCalculable(context["BiasY"].string!!)
            autoSize = context["AutoSize"].bool == true
            if (!autoSize) {
                width = DoubleCalculable(context["Width"].string!!)
                height = DoubleCalculable(context["Height"].string!!)
            }
            else {
                width = null
                height = null
            }
        }
        else {
            biasX = null
            biasY = null
            autoSize = true
            width = null
            height = null
        }
    }
    private val parse = context["Parse"].string ?: "Pre"


    override fun onEnter(base: CSExecutor) {
        when (type) {
            0 -> {
                base.appendRenderer(BackgroundRenderer(image, biasX, biasY, width, height, customPos, autoSize, parse))
            }
            1 -> {
                base.clearRenderer(CSExecutor.RenderParse.Pre)
                base.appendRenderer(BackgroundRenderer(image, biasX, biasY, width, height, customPos, autoSize, parse))
            }
            2 -> {
                poppedRenderer.push(base.popRenderer(CSExecutor.RenderParse.Pre))
            }
        }
    }

    private val poppedRenderer = Stack<ScriptRenderer>()
    override fun onRevert(base: CSExecutor) {
        super.onRevert(base)
        if (type == 2)
            base.appendRenderer(poppedRenderer.pop())
        else base.popRenderer(CSExecutor.RenderParse.Pre)
    }
}