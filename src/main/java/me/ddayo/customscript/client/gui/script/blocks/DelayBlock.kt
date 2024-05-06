package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.DoubleCalculable
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string


object DelayBlockInitializer: BlockInitializer {
    override val name = "DelayBlock"
    override fun initialize(context: Option) = DelayBlock(context)
}

class DelayBlock(context: Option): PendingBlock(context), ICalculableHolder {
    private var enterTime = -1L
    private var key = ""
    private val timeLimit: DoubleCalculable?
    private val type = when (context["Type"].string) {
        "Key" -> {
            key = context["Key"].string!!
            timeLimit = null
            0
        }

        "Time" -> {
            timeLimit = DoubleCalculable(context["Time"].string!!)
            1
        }

        else -> throw CompileError("Not supported value on DelayBlock")
    }

    override fun validateKeyInput(base: CSExecutor, keyCode: Int, scanCode: Int, modifier: Int) = when (type) {
        0 -> if (key.last() == '^' && key.all {
                if (it in '0'..'9') base.numberState[it.digitToInt()]
                else base.alphabetState[it.code - 'A'.code]
            }) PendingResult.Applied<PendingBlock>(this) else PendingResult.Yield()

        1 -> PendingResult.Yield()
        else -> PendingResult.Yield()
    }

    override fun validateMouseInput(base: CSExecutor, mouseX: Double, mouseY: Double, mouseButton: Int) = when (type) {
        0 -> if (mouseButton.digitToChar() == key.last() && key.all {
                if (it in '0'..'9') base.numberState[it.digitToInt()]
                else base.alphabetState[it.code - 'A'.code]
            }) PendingResult.Applied<PendingBlock>(this) else PendingResult.Yield()

        1 -> PendingResult.Yield()
        else -> PendingResult.Yield()
    }

    override fun onEnter(base: CSExecutor) {
        super.onEnter(base)
        enterTime = System.currentTimeMillis()
    }

    override fun tick() = when (type) {
        0 -> PendingResult.Yield()
        1 -> if (System.currentTimeMillis() - enterTime > timeLimit!!.get * 1000L) PendingResult.Applied<PendingBlock>(this) else PendingResult.Yield()
        else -> PendingResult.Yield()
    }

    override val calculable by lazy { if(timeLimit != null) listOf(timeLimit) else listOf() }
}