package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.ImageResource
import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.DoubleCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.bool
import me.ddayo.customscript.util.options.Option.Companion.string
import me.ddayo.goosegooseduck.client.math.Area
import me.ddayo.goosegooseduck.client.math.Point.Companion.with


object ButtonBlockInitializer: BlockInitializer {
    override val name = "ButtonBlock"
    override fun initialize(context: Option) = ButtonBlock(context)
}

class ButtonBlock(context: Option) : PendingBlock(context) {
    private class ButtonRenderer(
        private val buttonX: DoubleCalculable,
        private val buttonY: DoubleCalculable,
        private val buttonWidth: DoubleCalculable,
        private val buttonHeight: DoubleCalculable,
        private val buttonImage: StringCalculable,
        private val autoSize: Boolean
    ) : ScriptRenderer(), ICalculableHolder {
        override fun RenderUtil.renderInternal() {
            if (buttonImage.get.isNotBlank())
                push {
                    useTexture(ImageResource.getOrCreate(buttonImage.get)) {
                        if (autoSize)
                            render(buttonX.get.toInt(), buttonY.get.toInt(), getTexWidth(), getTexHeight())
                        else render(buttonX.get, buttonY.get, buttonWidth.get, buttonHeight.get)
                    }
                }
        }

        override val renderParse: CSExecutor.RenderParse
            get() = CSExecutor.RenderParse.Main
        override val isLoading: Boolean
            get() = !ImageResource.getOrCreate(buttonImage.get).isLoaded

        override val calculable by lazy { listOf(buttonX, buttonY, buttonWidth, buttonHeight, buttonImage) }
    }

    private val buttonX = DoubleCalculable(context["ButtonX"].string!!)
    private val buttonY = DoubleCalculable(context["ButtonY"].string!!)
    private val buttonWidth = DoubleCalculable(context["ButtonWidth"].string!!)
    private val buttonHeight = DoubleCalculable(context["ButtonHeight"].string!!)
    private val buttonImage = StringCalculable(context["ButtonImage"].string!!)
    private val autoSize = context["AutoSize"].bool ?: false
    private val clickArea = context["AdvancedSize"].string?.let { ac ->
        Area(*ac.split('|').map {
            val a = it.split(',').map { v -> v.toDouble() }.toList()
            a[0] with a[1]
        }.toTypedArray())
    }

    override val rendererInstance: ScriptRenderer
        get() = ButtonRenderer(buttonX, buttonY, buttonWidth, buttonHeight, buttonImage, autoSize)

    override fun validateKeyInput(gui: CSExecutor, keyCode: Int, scanCode: Int, modifier: Int) = PendingResult.Deny<PendingBlock>()

    override fun validateMouseInput(gui: CSExecutor, mouseX: Double, mouseY: Double, mouseButton: Int) =
        PendingResult.trueThenPass<PendingBlock>(
            clickArea?.isIn((mouseX with mouseY) - (buttonX.get with buttonY.get))
                ?: (mouseX in buttonX.get..(buttonX.get + buttonWidth.get) && mouseY in buttonY.get..(buttonY.get + buttonHeight.get)),
            this
        )

}