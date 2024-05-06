package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.js.StringCalculable
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string


object ModifyVariableBlockInitializer: BlockInitializer {
    override val name = "ModifyVariableBlock"
    override fun initialize(context: Option) = ModifyVariableBlock(context)
}

class ModifyVariableBlock(context: Option): BlockBase(context), ICalculableHolder {
    val name = StringCalculable(context["Name"].string!!)
    val value = StringCalculable(context["Value"].string!!)

    override fun onEnter(base: CSExecutor) {
        super.onEnter(base)
        CalculableValueManager.setValue(name.get, value.get)
    }

    override val calculable by lazy { listOf(name, value) }
}