package me.ddayo.customscript.client.gui.script.blocks

import me.ddayo.customscript.client.gui.script.CSExecutor
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.options.Option

abstract class PendingBlock(context: Option) : BlockBase(context) {
    open fun validateKeyInput(base: CSExecutor, keyCode: Int, scanCode: Int, modifier: Int): PendingResult<PendingBlock> = PendingResult.Yield()
    open fun validateMouseInput(base: CSExecutor, mouseX: Double, mouseY: Double, mouseButton: Int): PendingResult<PendingBlock> = PendingResult.Yield()

    open fun onExitPending() {
        if (this is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.remove(this)
    }

    override fun onEnter(base: CSExecutor) {
        super.onEnter(base)
        if (this is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.add(this)
    }

    open fun tick(): PendingResult<PendingBlock> = PendingResult.Yield()
}

sealed class PendingResult<T>(val toContinue: Boolean) {
    companion object {
        fun<T> trueThenPass(b: Boolean, rst: T) = if(b) Applied(rst) else Yield()

        fun<T> execute(r: PendingResult<T>, switch: PendingSwitch<T>): Boolean {
            when(r) {
                is Applied<T> -> switch.applied(r.value)
                is ProcessedList<T> -> switch.processed(r.values)
                is Processed -> switch.processed(listOf(r.value))
                is Yield<*> -> switch.yield()
                is Deny<*> -> switch.deny()
            }
            return r.toContinue
        }

        fun<T> execute(l: List<ResultExecutable<T>>, switch: PendingSwitch<T>) = execute(process(l), switch)
        fun<T, R> execute(l: List<T>, switch: PendingSwitch<R>, f: (T) -> PendingResult<R>) = execute(process(l, f), switch)

        private fun<T> process(l: List<ResultExecutable<T>>) : PendingResult<T> {
            val processed = mutableListOf<T>()
            l.forEach {
                val exr = it.execute()
                when(exr) {
                    is Applied<T> -> return exr
                    is Processed<T> -> processed.add(exr.value)
                    is Yield<T> -> {}
                    is Deny<T> -> return exr
                    else -> throw IllegalStateException()
                }
            }
            return if(processed.isEmpty()) Yield() else ProcessedList(processed)
        }

        fun<T, R> process(l: List<T>, f: (T) -> PendingResult<R>): PendingResult<R> {
            val processed = mutableListOf<R>()
            l.forEach {
                val exr = f(it)
                when(exr) {
                    is Applied<R> -> return exr
                    is Processed<R> -> processed.add(exr.value)
                    is Yield<R> -> {}
                    is Deny<R> -> return exr
                    else -> throw IllegalStateException()
                }
            }
            return if(processed.isEmpty()) Yield() else ProcessedList(processed)
        }
    }

    data class Applied<T>(val value: T): PendingResult<T>(false)
    class Yield<T>: PendingResult<T>(true)
    class Deny<T>: PendingResult<T>(false)
    class Processed<T>(val value: T): PendingResult<T>(false)
    private class ProcessedList<T>(val values: List<T>): PendingResult<T>(false)
}

interface PendingSwitch<T> {
    fun applied(d: T) {

    }
    fun yield() {

    }
    fun processed(d: List<T>) {

    }
    fun deny() {

    }
}

fun interface ResultExecutable<T> {
    fun execute(): PendingResult<T>
}
