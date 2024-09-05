package me.ddayo.customscript.util.lua

import me.ddayo.customscript.client.gui.RenderUtil
import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.lua.renderer.LuaImageRenderer
import me.ddayo.customscript.util.lua.renderer.LuaTextRenderer
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.value.LuaValue
import kotlin.math.min

open class RenderableLuaEngine(coreScriptDirStr: String): BaseLuaEngine(coreScriptDirStr) {
    class LuaRenderer {
        private val fn = mutableListOf<RenderUtil.() -> Unit>()

        // those functions are directly called from Lua script
        fun pushStack() = fn.add { push() }
        fun popStack() = fn.add { pop() }
        fun translate(a: LuaValue, b: LuaValue, c: LuaValue) {
            val la = LuaOrInvoke.intoA<Double>(a)
            val lb = LuaOrInvoke.intoA<Double>(b)
            val lc = LuaOrInvoke.intoA<Double>(c)
            fn.add {
                translate(la(), lb(), lc())
            }
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

        fun text(text: LuaValue, x: LuaValue, y: LuaValue, z: LuaValue, scale: LuaValue, color: LuaValue, font: String) {
            val a = LuaOrInvoke.intoA<String>(text)
            val b = LuaOrInvoke.intoA<Double>(x)
            val c = LuaOrInvoke.intoA<Double>(y)
            val d = LuaOrInvoke.intoA<Double>(scale)
            val e = LuaOrInvoke.intoA<Number>(color)
            val zv = LuaOrInvoke.into<Double>(z)
            val cr = LuaTextRenderer(a, b, c, d, font, e, CSExecutor.RenderParse.Main)
            fn.add {
                push {
                    zv()?.let { translate(0.0, 0.0, it) }
                    cr.render(this)
                }
            }
        }

        fun image(image: LuaValue, x: LuaValue, y: LuaValue, z: LuaValue, width: LuaValue, height: LuaValue, parse: String) {
            val a = LuaOrInvoke.intoA<String>(image)
            val b = LuaOrInvoke.into<Double>(x)
            val c = LuaOrInvoke.into<Double>(y)
            val d = LuaOrInvoke.into<Double>(width)
            val e = LuaOrInvoke.into<Double>(height)
            val zv = LuaOrInvoke.into<Double>(z)
            val cr = LuaImageRenderer(a, b, c, d, e, parse)
            fn.add {
                push {
                    zv()?.let { translate(0.0, 0.0, it) }
                    cr.render(this)
                }
            }
        }

        fun popOp(count: Int) {
            repeat(min(fn.size, count)) { fn.removeLast() }
        }

        fun render(renderer: RenderUtil) {
            fn.forEach {
                renderer.push {
                    renderer.it()
                }
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
