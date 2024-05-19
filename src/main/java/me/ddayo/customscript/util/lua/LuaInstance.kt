package me.ddayo.customscript.util.lua

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.util.math.vector.Quaternion
import org.apache.logging.log4j.LogManager
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.Lua
import party.iroiro.luajava.Lua.LuaError
import party.iroiro.luajava.luajit.LuaJit
import party.iroiro.luajava.value.LuaValue
import java.io.File



open class LuaScriptInstance(name: String, script: String, engine: LuaEngine) {
    private val lua = engine.newThread()
    var finished = false
        private set

    private val ev by lazy { lua.get() }

    init {
        LogManager.getLogger().info("Start script execution")

        lua.getGlobal("load_scx")
        lua.push(script)
        lua.push(name)
        lua.push {
            ev
            0
        }
        yieldOrError { lua.resume(3) }

        while(true) {
            (ev["csx"]["renderer"].toJavaObject() as RenderableLuaEngine.LuaRenderer).apply(MatrixStack())

            if(finished) break
            invokeTick()

            if(finished) break
            invokeOnMouseRelease(1.5, 1.5)
        }
    }

    fun proceed() {
        yieldOrError { lua.resume(0) }
    }

    fun invokeTick() = invokeEvent("tick")

    fun invokeOnMouseRelease(mx: Double, my: Double) = invokeEvent("on_mouse_release") {
        lua.getGlobal("cmath")
        lua.getField(-1, "point")
        lua.getField(-1, "new")
        // push cmath.point
        lua.pushValue(-2)
        // first argument
        lua.push(mx)
        // second argument
        lua.push(my)
        // last argument
        lua.pushNil()
        // because `self` exists, call with 4 args
        orError { lua.pCall(4, 1) }
        // remove `point`, [-1] is the point value to pass.
        lua.remove(-2)
        // remove 'cmath`
        lua.remove(-2)
        lua.push(0)
        2
    }

    fun invokeEvent(name: String, f: (Lua) -> Int = { 0 }) {
        lua.push { lua ->
            lua.getField(1, "csx")
            lua.getField(-1, "invoke_event")
            lua.insert(-2)

            lua.push(name)
            val pushed = f(lua)
            orError {
                lua.pCall(pushed + 2, 0)
            }
            1
        }
        yieldOrError { lua.resume(1) }
    }

    private inline fun orError(f: Lua.() -> LuaError) {
        val l = f(lua)
        if(l != LuaError.OK) {
            LogManager.getLogger().error("$l, ${lua.toString(-1)}")
            throw Exception()
        }
    }
    private inline fun yieldOrError(f: Lua.() -> LuaError) {
        val l = f(lua)
        if(l == LuaError.OK)
            finished = true
        else if(l != LuaError.YIELD) {
            LogManager.getLogger().error("$l, ${lua.toString(-1)}")
            throw Exception()
        }
    }
}

object LuaOrInvoke {
    fun<T> into(v: LuaValue): () -> T? {
        if(v.type() == Lua.LuaType.FUNCTION) return { v.call()?.get(0)?.toJavaObject() as? T }
        val fx = v.toJavaObject() as? T
        return { fx }
    }
}

object FromServer {
    val dt = mutableMapOf<String, Any>()
    init {
        dt["test"] = 12
        dt["15"] = 15
    }

    @JvmStatic
    fun get(v: String): Any? {
        return dt[v]
    }
}

open class RenderableLuaEngine(coreScriptDirStr: String, syncFromServer: Boolean = false): LuaEngine(coreScriptDirStr, syncFromServer) {
    class LuaRenderer {
        private val fn = mutableListOf<(MatrixStack) -> Unit>()
        fun pushStack() = fn.add { it.push() }
        fun popStack() = fn.add { it.pop() }
        fun translate(a: LuaValue, b: LuaValue, c: LuaValue) {
            val la = LuaOrInvoke.into<Double>(a)
            val lb = LuaOrInvoke.into<Double>(b)
            val lc = LuaOrInvoke.into<Double>(c)
            fn.add { it.translate(la()!!, lb()!!, lc()!!) }
        }
        fun rotate(a: LuaValue, b: LuaValue, c: LuaValue) {
            val la = LuaOrInvoke.into<Float>(a)
            val lb = LuaOrInvoke.into<Float>(b)
            val lc = LuaOrInvoke.into<Float>(c)
            fn.add { it.rotate(Quaternion(la()!!, lb()!!, lc()!!, true)) }
        }
        fun scale(a: LuaValue, b: LuaValue, c: LuaValue) {
            val la = LuaOrInvoke.into<Float>(a)
            val lb = LuaOrInvoke.into<Float>(b)
            val lc = LuaOrInvoke.into<Float>(c)
            fn.add { it.scale(la()!!, lb()!!, lc()!!) }
        }

        fun apply(matrixStack: MatrixStack) {
            fn.forEach {
                it(matrixStack)
            }
        }
    }

    init {
        loadCoreExtensionWithName("csx", "gui/csx")
    }
}

open class LuaEngine(coreScriptDirStr: String, syncFromServer: Boolean = false) {
    private val coreScriptDir = File(coreScriptDirStr)
    private val mainScript = File(coreScriptDir, "csx_main.lua")
    protected val lua = LuaJit()
    init {
        lua.openLibraries()
        lua.push(coreScriptDirStr)
        lua.setGlobal("base_dir")

        LogManager.getLogger().info("Loading main Script")
        orError {
            load(mainScript.readText())
        }
        orError {
            pCall(0, 0)
        }
        loadCoreApi("list")
        loadCore("log")
        loadCore("csx")
        loadCoreApiWithName("cmath", "csx_math")
        shareGlobal("List")
        shareGlobal("cmath")
        shareGlobal("logger")

        if (syncFromServer) {
            loadCoreApiWithName("synced", "synced/csx")
            shareGlobal("from_server")
        }
    }

    public var finalized = false
        private set

    protected fun loadCore(name: String) = loadCoreWithName(name, name)
    protected fun loadCoreApi(name: String) = loadCoreApiWithName(name, name)
    protected fun loadCoreExtensionWithName(name: String, fd: String) = loadCoreExtension(name, File(coreScriptDir, "$fd.lua").readText())
    protected fun loadCoreWithName(name: String, fd: String) = loadCore(name, File(coreScriptDir, "$fd.lua").readText())
    protected fun loadCoreApiWithName(name: String, fd: String) = loadCoreApi(name, File(coreScriptDir, "$fd.lua").readText())

    protected inline fun notFinalized(f: Lua.()->LuaError) {
        if(finalized) throw IllegalStateException("Cannot load core file after script executed")
        orError(f)
    }

    protected fun loadCore(name: String, script: String) {
        LogManager.getLogger().info("Loading core library $name")
        notFinalized {
            getGlobal("load_core")
            push(script)
            push(name)
            pCall(2, 0)
        }
    }

    protected fun loadCoreExtension(name: String, script: String) {
        LogManager.getLogger().info("Loading extension for $name")
        notFinalized {
            getGlobal("load_core_extension")
            push(script)
            push(name)
            pCall(2, 0)
        }
    }

    protected fun loadCoreApi(name: String, script: String) {
        LogManager.getLogger().info("Loading core api $name")
        notFinalized {
            getGlobal("load_core_api")
            push(script)
            push(name)
            pCall(2, 0)
        }
    }

    protected fun shareGlobal(name: String) {
        notFinalized {
            getGlobal("share_global")
            push(name)
            pCall(1, 0)
        }
    }

    fun newThread(): AbstractLua {
        finalized = true
        return lua.newThread()
    }

    protected inline fun orError(f: Lua.() -> LuaError) {
        val l = f(lua)
        if(l != LuaError.OK)
            LogManager.getLogger().error("$l, ${lua.toString(-1)}")
    }
}