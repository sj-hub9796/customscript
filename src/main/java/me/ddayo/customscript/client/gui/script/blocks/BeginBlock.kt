package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.string

class BeginBlock(context: Option): BlockBase(context) {
    val label = context["Label"].string!!
}

object BeginBlockInitializer: BlockInitializer {
    override val name = "BeginBlock"
    override fun initialize(context: Option) = BeginBlock(context)
}