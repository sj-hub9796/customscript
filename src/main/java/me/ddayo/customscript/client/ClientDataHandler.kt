package me.ddayo.customscript.client

import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.client.gui.script.ScriptMode
import me.ddayo.customscript.client.gui.script.StackableScriptScreen
import me.ddayo.customscript.util.js.CalculableValueManager
import net.minecraft.client.Minecraft

object ClientDataHandler {

    fun updateDynamicValue(key: String, value: String) = CalculableValueManager.setValue(key, value)

    internal val enabledHud = emptyMap<String, CSExecutor>().toMutableMap()

    fun addScreen(sc: CSExecutor) {
        val cs = Minecraft.getInstance().currentScreen
        if(cs is StackableScriptScreen)
            cs.pushScreen(sc)
        else Minecraft.getInstance().displayGuiScreen(StackableScriptScreen(ScriptMode.Gui).apply {
            pushScreen(sc)
        })
    }
}
