package me.ddayo.customscript.client.script

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.junit.jupiter.api.Test

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
}