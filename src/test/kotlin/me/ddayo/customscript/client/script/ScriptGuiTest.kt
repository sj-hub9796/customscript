package me.ddayo.customscript.client.script

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import me.ddayo.customscript.util.lua.BaseLuaEngine
import me.ddayo.customscript.util.lua.ScriptableInstance
import org.junit.jupiter.api.Test
import java.io.File

internal class ScriptGuiTest {
    @Test
    public fun parsingTest() {
        val js = NashornScriptEngineFactory().getScriptEngine("-scripting")
        js.eval("print('asdf')")
        js.put("asdf", 12)
        js.eval("print(asdf)")
        println(js.get("asdf"))
        println(js.eval("asdf") as Int)
        println(js.eval("0.1") as Float)
        js.eval("print(\"\${asdf + 12}\")")
    }

    @Test
    public fun luaTest() {
        val coreScriptDirStr = "/Users/dayo/IdeaProjects/customscript16/src/main/java/me/ddayo/customscript/util/lua/"
        // val coreScriptDirStr = "C:/Users/dayo/Desktop/customscript/src/main/java/me/ddayo/customscript/util/lua"

        val engine = BaseLuaEngine(coreScriptDirStr)
        val sc = engine.runScript("name", File(coreScriptDirStr, "cs_out.lua").readText()) as ScriptableInstance
        while(true) {
            Thread.yield()
            sc.invokeEvent("tick")
            if(sc.finished) break
        }
    }
}