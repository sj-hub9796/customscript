package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string


object JavaScriptBlockInitializer: BlockInitializer {
    override val name = "JavaScriptBlock"
    override fun initialize(context: Option) = JavaScriptBlock(context)
}

class JavaScriptBlock(context: Option): BlockBase(context) {
    var script = context["script"].string!!

    override fun onEnter(base: CSExecutor) {
        super.onEnter(base)
        CalculableValueManager.execute(script)
    }
}