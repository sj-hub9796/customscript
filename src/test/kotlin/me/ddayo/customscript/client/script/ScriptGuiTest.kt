package me.ddayo.customscript.client.script

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import me.ddayo.customscript.util.lua.LuaEngine
import me.ddayo.customscript.util.lua.LuaScriptInstance
import me.ddayo.customscript.util.lua.RenderableLuaEngine
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
        // val coreScriptDirStr = "/Users/dayo/IdeaProjects/customscript16/src/main/java/me/ddayo/customscript/util/lua/"
        val coreScriptDirStr = "C:/Users/dayo/Desktop/customscript/src/main/java/me/ddayo/customscript/util/lua"

        val engine = RenderableLuaEngine(coreScriptDirStr, true)
        val script = LuaScriptInstance("name", File("C:\\Users\\dayo\\Desktop\\customscript\\src\\main\\java\\me\\ddayo\\customscript\\util\\lua\\example.lua").readText(), engine)
    }
}