package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.int
import me.ddayo.customscript.util.options.Option.Companion.string


interface BlockInitializer {
    val name: String
    val contextName: String
        get() = name + "Context"
    fun initialize(context: Option): BlockBase
}

abstract class BlockBase(context: Option) {
    open fun onEnter(base: CSExecutor) {
        if (this is ICalculableHolder)
            recalculateAll()
        renderer?.let {
            base.appendRenderer(it)
        }
    }
    open fun onRevert(base: CSExecutor) {
        renderer?.let {
            base.popRenderer(it)
        }
    }

    open val rendererInstance: ScriptRenderer? = null
    protected val renderer by lazy { rendererInstance }

    val ns = context["NS"].int!!

    companion object {
        private val blockInitializers = mutableMapOf<String, BlockInitializer>()
        fun registerInitializer(vararg initializer: BlockInitializer) {
            initializer.forEach {
                blockInitializers[it.name] = it
            }
        }

        fun createBlock(name: String, context: Option): BlockBase {
            if (!blockInitializers.containsKey(name)) throw CompileError("Not supported block: $name")
            if (context["Context"].string != blockInitializers[name]!!.contextName) throw IllegalArgumentException("Context type ${context["Context"].first()} and declared context type ${blockInitializers[name]!!.contextName} are different")
            return blockInitializers[name]!!.initialize(context["Context"].first())
        }

        init {
            registerInitializer(
                BeginBlockInitializer,
                ButtonBlockInitializer,
                ChangeBackgroundBlockInitializer,
                DelayBlockInitializer,
                JavaScriptBlockInitializer,
                ModifyVariableBlockInitializer,
                RenderItemBlockInitializer,
                RunCommandBlockInitializer,
                TextBlockInitializer
            )
        }
    }
}