package me.ddayo.customscript.util.lua

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.lua.renderer.LuaTextRenderer
import net.minecraft.util.math.vector.Matrix4f
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.value.LuaValue

open class RenderableLuaEngine(coreScriptDirStr: String): BaseLuaEngine(coreScriptDirStr) {
    class LuaRenderer {
        private val fn = mutableListOf<RenderUtil.() -> Unit>()
        fun pushStack() = fn.add { push() }
        fun popStack() = fn.add { pop() }
        fun translate(a: LuaValue, b: LuaValue, c: LuaValue) {
            val la = LuaOrInvoke.intoA<Double>(a)
            val lb = LuaOrInvoke.intoA<Double>(b)
            val lc = LuaOrInvoke.intoA<Double>(c)
            fn.add { translate(la(), lb(), lc()) }
        }

        fun rotate(a: LuaValue, b: LuaValue, c: LuaValue) {
            val la = LuaOrInvoke.intoA<Double>(a)
            val lb = LuaOrInvoke.intoA<Double>(b)
            val lc = LuaOrInvoke.intoA<Double>(c)
            fn.add { rotate(la(), lb(), lc()) }
        }

        fun scale(a: LuaValue, b: LuaValue, c: LuaValue) {
            val la = LuaOrInvoke.intoA<Double>(a)
            val lb = LuaOrInvoke.intoA<Double>(b)
            val lc = LuaOrInvoke.intoA<Double>(c)
            fn.add { scale(la(), lb(), lc()) }
        }

        fun text(text: LuaValue, x: LuaValue, y: LuaValue, scale: LuaValue, color: LuaValue, font: String) {
            var pvm: Matrix4f? = null
            val a = LuaOrInvoke.intoA<String>(text)
            val b = LuaOrInvoke.intoA<Double>(x)
            val c = LuaOrInvoke.intoA<Double>(y)
            val d = LuaOrInvoke.intoA<Double>(scale)
            val e = LuaOrInvoke.intoA<Number>(color)
            var cr: LuaTextRenderer? = null
            fn.add {
                if(pvm != matrix.last.matrix) {
                    pvm = matrix.last.matrix
                    cr = LuaTextRenderer(a, b, c, d, font, e, CSExecutor.RenderParse.Main)
                }
                cr?.render(this)
            }
        }

        fun render(renderer: RenderUtil) {
            fn.forEach {
                renderer.it()
            }
        }
    }

    init {
        loadCoreExtensionWithName("csx", "gui/csx")
    }

    override fun newScriptHandler(name: String, script: String, newLua: AbstractLua): ILuaScriptHandler {
        return RenderableScriptInstance(name, script, newLua)
    }
}

open class RenderableScriptInstance(name: String, script: String, lua: AbstractLua): ScriptableInstance(name, script, lua) {
    fun invokeTick() = invokeEvent("tick")
}
