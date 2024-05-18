package me.ddayo.customscript.client.gui.script

import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.client.gui.script.arrows.Arrow
import me.ddayo.customscript.client.gui.script.blocks.*
import me.ddayo.customscript.network.CloseGuiNetworkHandler
import me.ddayo.customscript.util.js.CalculableValueManager
import me.ddayo.customscript.util.js.ICalculableHolder
import me.ddayo.customscript.util.options.CompileError
import me.ddayo.customscript.util.options.Option
import me.ddayo.customscript.util.options.Option.Companion.bool
import me.ddayo.customscript.util.options.Option.Companion.string
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.LogManager
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.io.File
import java.util.*


interface ScriptFrame {
    val currentBlock: BlockBase
}

data class SingleFrame(override val currentBlock: BlockBase, val nextFrame: Lazy<ScriptFrame>) : ScriptFrame

data class PendingFrame(override val currentBlock: BlockBase, val nextFrames: List<Lazy<ScriptFrame>>) : ScriptFrame

data class LastFrame(override val currentBlock: BlockBase) : ScriptFrame

class CSExecutor(private var current: ScriptFrame, global: Option, private val syncRequired: Boolean) {
    enum class RenderParse {
        Pre, Main, Post
    }

    companion object {
        val minimumRequiredVersion = DefaultArtifactVersion("0.8")

        fun fromFile(scriptFile: String, beginPos: String, syncRequired: Boolean): CSExecutor? {
            val scriptFileRoot =
                if (CustomScript.isTest) File("dummy") else File(Minecraft.getInstance().gameDir, CustomScript.MOD_ID)
            val scFile = if (CustomScript.isTest) File(scriptFile) else File(scriptFileRoot, scriptFile)
            if (!scFile.exists() || !scFile.isFile || !scFile.canRead()) {
                Minecraft.getInstance().player?.sendMessage(
                    StringTextComponent("Cannot open script: $scriptFile"),
                    Util.DUMMY_UUID
                )
                LogManager.getLogger().warn("Cannot open file: $scriptFile")
                return null
            }
            return fromString(scFile.readText(), beginPos, syncRequired)
        }

        fun fromString(script: String, beginPos: String, syncRequired: Boolean): CSExecutor {
            val opt = Option.readOption(script)
            return CSExecutor(optionToFrame(opt, beginPos), opt, syncRequired)
        }

        fun optionToFrame(script: Option, beginPos: String): ScriptFrame {
            val blocks = script["Block"].map { BlockBase.createBlock(it.value, it) }
            val arrows = script["Arrow"].map { Arrow.createArrow(it.value, it) }
            val frames = mutableMapOf<Int, ScriptFrame>()

            blocks.map { bl ->
                val cbl = arrows.filter { it.from == bl.ns }.map { blocks.first { cbl -> cbl.ns == it.to } }
                if (cbl.any { it is PendingBlock }) {
                    if (!cbl.all { it is PendingBlock })
                        throw CompileError("If pending block exists on the frame then all must be that.")
                    frames[bl.ns] = PendingFrame(bl, cbl.map { lazy { frames[it.ns]!! } })
                } else if (cbl.size >= 2) throw CompileError("All block must be pending block if target is multiple.")
                else if (cbl.size == 1) frames[bl.ns] = SingleFrame(bl, cbl.first().ns.let { lazy { frames[it]!! } })
                else frames[bl.ns] = LastFrame(bl)
            }

            return frames[blocks.first { it is BeginBlock && it.label == beginPos }.ns]!!
        }
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
        if (minimumRequiredVersion > DefaultArtifactVersion(global["Version"].string)) {
            Minecraft.getInstance().player?.sendMessage(
                StringTextComponent("Not supported version: ${global["Version"].string}, Required: $minimumRequiredVersion"),
                UUID.randomUUID()
            )
            throw CompileError("Not supported version: ${global["Version"].string}, Required: $minimumRequiredVersion")
        }
    }

    private val canMovePrevious = global["CanMovePrevious"].bool ?: false

    val renderable =
        mapOf(*RenderParse.values().map { Pair(it, emptyList<ScriptRenderer>().toMutableList()) }.toTypedArray())

    private val trackPrev = Stack<Int>()

    fun process() {
        while(current is SingleFrame) {
            trackPrev.push(current.currentBlock.ns)
            current = (current as SingleFrame).nextFrame.value
            current.currentBlock.onEnter(this)
        }
        if(current is LastFrame)
            finish()
        else (current as? PendingFrame)?.nextFrames?.forEach {
            it.value.currentBlock.onEnter(this)
        }
    }

    init {
        process()
    }

    fun revert() {
        if(current !is SingleFrame) return

    }

    /*
    fun movePrev() {
        if (!pending) return
        current.forEach { (it as PendingBlock).onExitPending() }
        while (true) {
            val b = blocks.first { it.ns == trackPrev.peek() }
            if (b is PendingBlock) {
                trackPrev.pop()
                val cached = arrows.filter { it.from == trackPrev.peek() }.map { it.to }
                current = blocks.filter { cached.contains(it.ns) }
                break
            }
            b.onRevert(this)
            trackPrev.pop()
        }
    }

     */

    val isPending get() = current is PendingFrame

    fun cancelPending(to: Int) {
        if(current !is PendingFrame) throw IllegalStateException("Function is not pending")
        val pf = (current as PendingFrame)
        if(!pf.nextFrames.any { it.value.currentBlock.ns == to })
            throw IllegalStateException("Not able to load $to")

        pf.nextFrames.forEach {
            (it.value.currentBlock as? PendingBlock)?.onExitPending() ?: throw IllegalStateException("Not pending block")
        }

        clearRenderer(RenderParse.Main)
        clearRenderer(RenderParse.Post)

        trackPrev.push(to)
        current = pf.nextFrames.first { it.value.currentBlock.ns == to }.value
        process()
    }

    private val pendingProcessor = object : PendingSwitch<PendingBlock> {
        override fun applied(d: PendingBlock) {
            cancelPending(d.ns)
        }
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): PendingResult<CSExecutor> {
        if (!isPending) return PendingResult.Deny()

        if (canMovePrevious && button == 1) {
            revert()
            return PendingResult.Applied(this)
        } else return if (PendingResult.execute((current as PendingFrame).nextFrames, pendingProcessor) {
                (it as PendingBlock).validateMouseInput(this, mouseX, mouseY, button)
            }) PendingResult.Yield() else PendingResult.Applied(this)
    }

    val alphabetState = MutableList('Z'.code - 'A'.code + 1) { false }
    val numberState = MutableList(10) { false }

    fun setKeyState(keyCode: Int, value: Boolean) {
        if (keyCode in 'A'.code..'Z'.code)
            alphabetState[keyCode - 'A'.code] = value
        else if (keyCode in '0'.code..'9'.code)
            numberState[keyCode - '0'.code] = value
    }

    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): PendingResult<CSExecutor> {
        setKeyState(keyCode, true)
        if (!isPending) return PendingResult.Deny()

        return if (PendingResult.execute((current as PendingFrame).nextFrames, pendingProcessor) {
                (it as PendingBlock).validateKeyInput(this, keyCode, scanCode, modifiers)
            }) PendingResult.Yield() else PendingResult.Applied(this)
    }

    fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): PendingResult<CSExecutor> {
        setKeyState(keyCode, false)

        if (!isPending) return PendingResult.Yield()
        return PendingResult.Yield() // TODO
    }

    fun tick(): PendingResult<CSExecutor> {
        if (!isPending) return PendingResult.Yield()
        return if (PendingResult.execute((current as PendingFrame).nextFrames, pendingProcessor) {
                (it as PendingBlock).tick()
            }) PendingResult.Yield() else PendingResult.Applied(this)
    }

    fun appendRenderer(block: ScriptRenderer) {
        renderable[block.renderParse]!!.add(block)
        if (block is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.add(block)
    }

    fun popRenderer(block: ScriptRenderer) = popRenderer(block.renderParse)
    fun popRenderer(parse: RenderParse): ScriptRenderer {
        val r = renderable[parse]!!.removeLast().apply {
            onRemovedFromQueue()
        }
        if (r is ICalculableHolder)
            CalculableValueManager.dynamicValueHolder.remove(r)
        return r
    }

    fun clearRenderer(block: ScriptRenderer) = clearRenderer(block.renderParse)
    fun clearRenderer(parse: RenderParse) {
        renderable[parse]!!.forEach { it.onRemovedFromQueue() }
        renderable[parse]!!.clear()
    }

    var isValid = true
        private set

    fun finish() {
        isValid = false
        if (syncRequired)
            CustomScript.network.sendToServer(CloseGuiNetworkHandler())

        RenderParse.values().forEach {
            clearRenderer(it)
        }

        MinecraftForge.EVENT_BUS.unregister(this)
    }
}