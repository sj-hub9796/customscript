package me.ddayo.customscript.util.lua

import org.apache.logging.log4j.LogManager
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.Lua
import party.iroiro.luajava.Lua.LuaError
import party.iroiro.luajava.luajit.LuaJit
import party.iroiro.luajava.value.LuaValue
import java.io.File


object LuaOrInvoke {
    fun <T> into(v: LuaValue): () -> T? {
        val lua = v.state()
        v.push()
        val ref = lua.ref()
        lua.pop(1)

        // TODO: call unref on gc
        if (v.type() == Lua.LuaType.FUNCTION)
            return {
                lua.refGet(ref)
                lua.pCall(0, 1)
                LogManager.getLogger().info(lua.get()?.toJavaObject())
                lua.get()?.toJavaObject() as? T
            }
        val fx = v.toJavaObject() as? T
        return { fx }
    }

    fun <T> intoA(v: LuaValue): () -> T {
        val lua = v.state()
        v.push()
        val ref = lua.ref()
        lua.pop(1)
        if (v.type() == Lua.LuaType.FUNCTION)
            return {
                lua.refGet(ref)
                LogManager.getLogger().info(lua.type(-1))
                lua.pCall(0, 1)
                (lua.get()?.toJavaObject() as? T)!!
            }
        val fx = v.toJavaObject() as? T
        return { fx!! }
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

open class ScriptableInstance(name: String, script: String, lua: AbstractLua) : ILuaScriptHandler(lua) {
    protected val ev: LuaValue by lazy { lua.get() }

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
    }

    fun proceed() {
        yieldOrError { lua.resume(0) }
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
}

open class BaseLuaEngine(coreScriptDirStr: String) :
    AbstractLuaEngine(coreScriptDirStr) {
    companion object {
        val CORE_DIR = "/Users/dayo/IdeaProjects/customscript16/src/main/java/me/ddayo/customscript/util/lua/"
    }

    init {
        loadCoreApi("list")
        loadCore("log")
        loadCore("csx")
        loadCoreApiWithName("cmath", "csx_math")

        shareGlobal("List")
        shareGlobal("cmath")
        shareGlobal("logger")

        loadCoreApiWithName("synced", "synced/csx")
        shareGlobal("from_server")
    }

    override fun newScriptHandler(name: String, script: String, newLua: AbstractLua): ILuaScriptHandler {
        return ScriptableInstance(name, script, newLua)
    }
}

abstract class AbstractLuaEngine(coreScriptDirStr: String) {
    protected val lua = LuaJit()
    protected var finalized = false
        private set

    private val coreScriptDir = File(coreScriptDirStr)
    private val mainScript = File(coreScriptDir, "csx_main.lua")

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
    }

    protected fun loadCore(name: String) = loadCoreWithName(name, name)
    protected fun loadCoreApi(name: String) = loadCoreApiWithName(name, name)
    protected fun loadCoreExtensionWithName(name: String, fd: String) =
        loadCoreExtension(name, File(coreScriptDir, "$fd.lua").readText())

    protected fun loadCoreWithName(name: String, fd: String) = loadCore(name, File(coreScriptDir, "$fd.lua").readText())
    protected fun loadCoreApiWithName(name: String, fd: String) =
        loadCoreApi(name, File(coreScriptDir, "$fd.lua").readText())

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

    protected inline fun notFinalized(f: Lua.() -> LuaError) {
        if (finalized) throw IllegalStateException("Cannot load core file after script executed")
        orError(f)
    }

    fun runScript(name: String, script: String): ILuaScriptHandler {
        finalized = true
        return newScriptHandler(name, script, lua.newThread())
    }

    protected abstract fun newScriptHandler(name: String, script: String, newLua: AbstractLua): ILuaScriptHandler

    protected inline fun orError(f: Lua.() -> LuaError) {
        val l = f(lua)
        if (l != LuaError.OK)
            throw LuaError("$l, ${lua.toString(-1)}")
    }
}

class LuaError(message: String? = null, base: Throwable? = null) : Exception(message, base)

abstract class ILuaScriptHandler(protected val lua: AbstractLua) {
    var finished = false
        protected set

    protected inline fun orError(f: Lua.() -> LuaError) {
        val l = f(lua)
        if (l != LuaError.OK)
            throw LuaError("$l, ${lua.toString(-1)}")
    }

    protected inline fun yieldOrError(f: Lua.() -> LuaError) {
        val l = f(lua)
        if (l == LuaError.OK)
            finished = true
        else if (l != LuaError.YIELD)
            throw LuaError("$l, ${lua.toString(-1)}")
    }
}
