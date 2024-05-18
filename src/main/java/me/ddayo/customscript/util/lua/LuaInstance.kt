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
        lua.getGlobal("load_scx")
        lua.push(script)
        lua.push(name)

        yieldOrError { lua.resume(2) }

        while(true) {
            if(finished) break
            invokeEvent("tick")

            invokeEvent("on_mouse_release") {
                lua.getGlobal("cmath")
                lua.getField(-1, "point")
                lua.getField(-1, "new")
                lua.insert(-2)
                lua.push(1.5)
                lua.push(1.5)
                orError { lua.pCall(3, 1) }
                lua.remove(-1)
                // orError { lua.load("return cmath.point:new(1.5, 1.5)") }
                // orError { lua.pCall(0,1) }
                lua.push(0)
                2
            }
        }
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
    private val coreScriptDirStr = "/Users/dayo/IdeaProjects/customscript16/src/main/java/me/ddayo/customscript/util/lua/"
    private val coreScriptDir = File(coreScriptDirStr)
    private val mainScript = File("/Users/dayo/IdeaProjects/customscript16/src/main/java/me/ddayo/customscript/util/lua/csx_main.lua")
    private val lua = LuaJit()
    init {
        lua.openLibraries()
        lua.push(coreScriptDirStr)
        lua.setGlobal("baseDir")
        orError {
            load(mainScript.readText())
        }
        orError {
            pCall(0, 0)
        }
        loadCoreApi("list")
        loadCore("log")
        loadCore("csx")
        loadCoreWithName("cmath", "csx_math")
    }

    public var finalized = false
        private set

    fun loadCore(name: String) = loadCoreWithName(name, name)
    fun loadCoreApi(name: String) = loadCoreApiWithName(name, name)
    fun loadCoreWithName(name: String, fd: String) = loadCore(name, File(coreScriptDir, "$fd.lua").readText())
    fun loadCoreApiWithName(name: String, fd: String) = loadCoreApi(name, File(coreScriptDir, "$fd.lua").readText())

    fun loadCore(name: String, script: String) {
        if(finalized) throw IllegalStateException("Cannot load core file after script executed")
        orError {
            getGlobal("load_core")
            push(script)
            push(name)
            pCall(2, 0)
        }
    }

    fun loadCoreApi(name: String, script: String) {
        if(finalized) throw IllegalStateException("Cannot load core api file after script executed")
        orError {
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