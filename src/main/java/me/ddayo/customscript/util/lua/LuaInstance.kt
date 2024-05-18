package me.ddayo.customscript.util.lua

import org.apache.logging.log4j.LogManager
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.Lua
import party.iroiro.luajava.Lua.LuaError
import party.iroiro.luajava.luajit.LuaJit
import java.io.File



class LuaScriptInstance(name: String, script: String, engine: LuaEngine) {
    private val lua = engine.newThread()
    var finished = false
        private set

    init {
        LogManager.getLogger().info("Start script execution")
        lua.getGlobal("load_scx")
        lua.push(script)
        lua.push(name)

        yieldOrError { lua.resume(2) }

        while(true) {
            if(finished) break
            invokeTick()

            if(finished) break
            invokeOnMouseRelease(1.0, 2.0)
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
        lua.pushValue(-2)
        lua.push(mx)
        lua.push(my)
        lua.pushNil()
        orError { lua.pCall(4, 1) }
        lua.remove(-2)
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

class LuaEngine {
    /// private val coreScriptDirStr = "/Users/dayo/IdeaProjects/customscript16/src/main/java/me/ddayo/customscript/util/lua/"
    private val coreScriptDirStr = "C:/Users/dayo/Desktop/customscript/src/main/java/me/ddayo/customscript/util/lua"
    private val coreScriptDir = File(coreScriptDirStr)
    private val mainScript = File(coreScriptDir, "csx_main.lua")
    private val lua = LuaJit()
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
        loadCoreExtensionWithName("csx", "csx/gui")
    }

    public var finalized = false
        private set

    fun loadCore(name: String) = loadCoreWithName(name, name)
    fun loadCoreApi(name: String) = loadCoreApiWithName(name, name)
    fun loadCoreExtensionWithName(name: String, fd: String) = loadCoreExtension(name, File(coreScriptDir, "$fd.lua").readText())
    fun loadCoreWithName(name: String, fd: String) = loadCore(name, File(coreScriptDir, "$fd.lua").readText())
    fun loadCoreApiWithName(name: String, fd: String) = loadCoreApi(name, File(coreScriptDir, "$fd.lua").readText())

    private inline fun notFinalized(f: Lua.()->LuaError) {
        if(finalized) throw IllegalStateException("Cannot load core file after script executed")
        orError(f)
    }

    fun loadCore(name: String, script: String) {
        LogManager.getLogger().info("Loading core library $name")
        notFinalized {
            getGlobal("load_core")
            push(script)
            push(name)
            pCall(2, 0)
        }
    }

    fun loadCoreExtension(name: String, script: String) {
        LogManager.getLogger().info("Loading extension for $name")
        notFinalized {
            getGlobal("load_core_extension")
            push(script)
            push(name)
            pCall(2, 0)
        }
    }

    fun loadCoreApi(name: String, script: String) {
        LogManager.getLogger().info("Loading core api $name")
        notFinalized {
            getGlobal("load_core_api")
            push(script)
            push(name)
            pCall(2, 0)
        }
    }

    fun newThread(): AbstractLua {
        finalized = true
        return lua.newThread()
    }

    private inline fun orError(f: Lua.() -> LuaError) {
        val l = f(lua)
        if(l != LuaError.OK)
            LogManager.getLogger().error("$l, ${lua.toString(-1)}")
    }
}