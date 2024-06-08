package me.ddayo.customscript.client.gui

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.platform.GlStateManager
import me.ddayo.customscript.CustomScript
import me.ddayo.customscript.util.native.NativeInstance
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.NativeImage
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Util
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.text.StringTextComponent
import org.apache.logging.log4j.LogManager
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL21
import org.lwjgl.opengl.GL33
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt


open class RenderUtilImpl : RenderUtil() {
    override fun readyRender() {

    }

    private fun bindTexture(idf: ResourceLocation) {
        Minecraft.getInstance().textureManager.bindTexture(idf)
    }

    override fun _useTexture(x: String) {
        bindTexture(ResourceLocation(CustomScript.MOD_ID, "textures/$x"))
    }

    override fun _useTexture(x: ImageResource) {
        x.bindTexture()
    }

    override fun unbindTexture() {
        bindTexture(ResourceLocation(CustomScript.MOD_ID, "textures/dummy.png"))
    }

    override fun resourceExists(x: String): Boolean {
        return Minecraft.getInstance().resourceManager.hasResource(ResourceLocation(CustomScript.MOD_ID, "textures/$x"))
    }

    override fun render(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        th1: Double,
        th2: Double,
        tv1: Double,
        tv2: Double
    ) = render(
        x.toFloat(),
        y.toFloat(),
        w.toFloat(),
        h.toFloat(),
        th1.toFloat(),
        th2.toFloat(),
        tv1.toFloat(),
        tv2.toFloat()
    )

    override fun fillRender(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        r: Int,
        g: Int,
        b: Int,
        a: Int
    ) = fillRender(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), r, g, b, a)

    override fun fillRender(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) = fillRender(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), r, g, b, a)

    fun fillRender(x: Float, y: Float, w: Float, h: Float, r: Int, g: Int, b: Int, a: Int) {
        Tessellator.getInstance().apply {
            buffer.apply {
                begin(GL21.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
                quads(x, y, w, h, r, g, b, a)
            }
            draw()
        }
    }

    fun fillRender(x: Float, y: Float, w: Float, h: Float, r: Float, g: Float, b: Float, a: Float) {
        Tessellator.getInstance().apply {
            buffer.apply {
                begin(GL21.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
                quadColor(x, y, w, h, r, g, b, a)
            }
            draw()
        }
    }

    fun render(x: Float, y: Float, w: Float, h: Float, th1: Float, th2: Float, tv1: Float, tv2: Float) {
        Tessellator.getInstance().apply {
            buffer.apply {
                begin(GL21.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
                quads(x, y, w, h, th1, th2, tv1, tv2)
            }
            draw()
        }
    }

    override fun renderColorTex(x: Float, y: Float, w: Float, h: Float, r: Float, g: Float, b: Float, a: Float) {
        Tessellator.getInstance().apply {
            buffer.apply {
                begin(GL21.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX)
                quadColorTex(x, y, w, h, 0.0f, 1.0f, 0.0f, 1.0f, r, g, b, a)
            }
            draw()
        }
    }

    private fun BufferBuilder.quads(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        th1: Float,
        th2: Float,
        tv1: Float,
        tv2: Float
    ) {
        pos(matrix.last.matrix, x, y + h, 0.0f).tex(th1, tv2).endVertex()
        pos(matrix.last.matrix, x + w, y + h, 0.0f).tex(th2, tv2).endVertex()
        pos(matrix.last.matrix, x + w, y, 0.0f).tex(th2, tv1).endVertex()
        pos(matrix.last.matrix, x, y, 0.0f).tex(th1, tv1).endVertex()
    }

    private fun BufferBuilder.quadColorTex(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        th1: Float,
        th2: Float,
        tv1: Float,
        tv2: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        pos(matrix.last.matrix, x, y + h, 0.0f).color(r, g, b, a).tex(th1, tv2).endVertex()
        pos(matrix.last.matrix, x + w, y + h, 0.0f).color(r, g, b, a).tex(th2, tv2).endVertex()
        pos(matrix.last.matrix, x + w, y, 0.0f).color(r, g, b, a).tex(th2, tv1).endVertex()
        pos(matrix.last.matrix, x, y, 0.0f).color(r, g, b, a).tex(th1, tv1).endVertex()
    }

    private fun BufferBuilder.quads(x: Float, y: Float, w: Float, h: Float, r: Int, g: Int, b: Int, a: Int) {
        pos(matrix.last.matrix, x, y + h, 0.0f).color(r, g, b, a).endVertex()
        pos(matrix.last.matrix, x + w, y + h, 0.0f).color(r, g, b, a).endVertex()
        pos(matrix.last.matrix, x + w, y, 0.0f).color(r, g, b, a).endVertex()
        pos(matrix.last.matrix, x, y, 0.0f).color(r, g, b, a).endVertex()
    }

    private fun BufferBuilder.quadColor(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        pos(matrix.last.matrix, x, y + h, 0.0f).color(r, g, b, a).endVertex()
        pos(matrix.last.matrix, x + w, y + h, 0.0f).color(r, g, b, a).endVertex()
        pos(matrix.last.matrix, x + w, y, 0.0f).color(r, g, b, a).endVertex()
        pos(matrix.last.matrix, x, y, 0.0f).color(r, g, b, a).endVertex()
    }

    override fun getTexWidth() = GL21.glGetTexLevelParameteri(GL21.GL_TEXTURE_2D, 0, GL21.GL_TEXTURE_WIDTH)

    override fun getTexHeight() = GL21.glGetTexLevelParameteri(GL21.GL_TEXTURE_2D, 0, GL21.GL_TEXTURE_HEIGHT)

    override fun push() = matrix.push()
    override fun pop() = matrix.pop()

    override fun translate(x: Double, y: Double, z: Double) = matrix.translate(x, y, z)

    override fun rotate(x: Double, y: Double, z: Double) =
        matrix.rotate(Quaternion(x.toFloat(), y.toFloat(), z.toFloat(), true))

    override fun scale(x: Double, y: Double, z: Double) = matrix.scale(x.toFloat(), y.toFloat(), z.toFloat())

    override fun _useTexture(x: Int) {
        GlStateManager.bindTexture(x)
    }

    private var swizzleTest = false
    private fun swizzleDisabledGrayscaleBuffer(buf: ByteBuffer, width: Int, height: Int): ByteBuffer {
        val dir = BufferUtils.createByteBuffer(width * height * 4)
        while(buf.hasRemaining()){
            val d = buf.get()
            dir.put(d)
            dir.put(d)
            dir.put(d)
            dir.put(d)
        }
        return dir
    }

    override fun bindGrayscaleBuffer(buf: ByteBuffer, width: Int, height: Int) {
        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_WRAP_S, GL21.GL_CLAMP_TO_EDGE)
        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_WRAP_T, GL21.GL_CLAMP_TO_EDGE)
        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_MIN_FILTER, GL21.GL_LINEAR)
        GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL21.GL_TEXTURE_MAG_FILTER, GL21.GL_LINEAR)

        if(!swizzleTest) {
            GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_R, GL33.GL_ALPHA)
            val gle = GL21.glGetError()
            if(gle != GL21.GL_NO_ERROR) { // test swizzle
                Minecraft.getInstance().player?.sendMessage(StringTextComponent("GPU 드라이버 문제가 감지되어 느린 방법으로 우회합니다."), Util.DUMMY_UUID)
                LogManager.getLogger().error("OpenGL error $gle: Cannot use auto swizzle with texture. On next try, it will uses cpu based method.")
                swizzleTest = true
            }
            else {
                GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_G, GL33.GL_ALPHA)
                GL21.glTexParameteri(GL21.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_B, GL33.GL_ALPHA)
            }
        }
        GL21.glPixelStorei(GL21.GL_UNPACK_ALIGNMENT, 1)

        GL21.glTexImage2D(
            GL21.GL_TEXTURE_2D,
            0,
            GL21.GL_RGBA,
            width,
            height,
            0,
            if(swizzleTest) GL21.GL_RGBA else GL21.GL_ALPHA,
            GL21.GL_UNSIGNED_BYTE,
            if(swizzleTest) swizzleDisabledGrayscaleBuffer(buf, width, height) else buf
        )
    }
}

abstract class RenderUtil {
    companion object {
        val renderer: RenderUtil = RenderUtilImpl()
    }

    fun useTexture(x: String, f: () -> Unit) {
        _useTexture(x)
        f()
        unbindTexture()
    }

    fun useTexture(x: ImageResource, f: () -> Unit) {
        _useTexture(x)
        f()
        unbindTexture()
    }

    fun useTexture(x: Int, f: () -> Unit) {
        _useTexture(x)
        f()
        unbindTexture()
    }

    open fun bindGrayscaleBuffer(buf: ByteBuffer, width: Int, height: Int) {
        throw NotImplementedError("bindGrayscaleBuffer(): Int not implemented")
    }

    protected abstract fun _useTexture(x: String)
    protected abstract fun _useTexture(x: ImageResource)
    protected open fun _useTexture(x: Int) {
        throw NotImplementedError("_useTexture(Int) not implemented")
    }

    protected abstract fun unbindTexture()

    abstract fun resourceExists(x: String): Boolean
    fun resourceExists(x: ImageResource): Boolean {
        return false
    }

    fun render() = render(0, 0, 1920, 1080)

    fun fillRender(x: Int, y: Int, w: Int, h: Int, r: Int, g: Int, b: Int, a: Int) =
        fillRender(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), r, g, b, a)

    fun fillRender(x: Int, y: Int, w: Int, h: Int, r: Double, g: Double, b: Double, a: Double) = fillRender(
        x.toDouble(),
        y.toDouble(),
        w.toDouble(),
        h.toDouble(),
        r.toFloat(),
        g.toFloat(),
        b.toFloat(),
        a.toFloat()
    )

    fun fillRender(x: Double, y: Double, w: Double, h: Double, r: Double, g: Double, b: Double, a: Double) =
        fillRender(x, y, w, h, r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())

    fun render(x: Int, y: Int, w: Int, h: Int) = render(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())

    fun render(x: Double, y: Double, w: Double, h: Double) = render(x, y, w, h, 0.0, 1.0, 0.0, 1.0)
    fun renderColorTex(x: Double, y: Double, w: Double, h: Double, r: Double, g: Double, b: Double, a: Double) =
        renderColorTex(
            x.toFloat(),
            y.toFloat(),
            w.toFloat(),
            h.toFloat(),
            r.toFloat(),
            g.toFloat(),
            b.toFloat(),
            a.toFloat()
        )

    abstract fun fillRender(x: Double, y: Double, w: Double, h: Double, r: Int, g: Int, b: Int, a: Int)
    abstract fun fillRender(x: Double, y: Double, w: Double, h: Double, r: Float, g: Float, b: Float, a: Float)
    abstract fun render(x: Double, y: Double, w: Double, h: Double, th1: Double, th2: Double, tv1: Double, tv2: Double)
    abstract fun renderColorTex(x: Float, y: Float, w: Float, h: Float, r: Float, g: Float, b: Float, a: Float)

    abstract fun getTexWidth(): Int
    abstract fun getTexHeight(): Int

    abstract fun push()
    abstract fun pop()
    fun push(f: () -> Unit) {
        push()
        f()
        pop()
    }

    abstract fun translate(x: Double, y: Double, z: Double)
    abstract fun rotate(x: Double, y: Double, z: Double)
    abstract fun scale(x: Double, y: Double, z: Double)
    open fun readyRender() {}

    fun FHDScale(width: Int, height: Int, x: () -> Unit) = FHDScale(width.toDouble(), height.toDouble(), x)

    fun FHDScale(width: Double, height: Double, x: () -> Unit) {
        push {
            translate((width - height * 16.0 / 9) / 2, 0.0, 0.0)
            scale(height / 1080.0, height / 1080.0, height / 1080.0)
            x()
        }
    }

    lateinit var matrix: MatrixStack
    fun loadMatrix(matrix: MatrixStack, f: RenderUtil.() -> Unit) {
        this.matrix = matrix
        this.f()
    }

    fun drawLine(sx: Double, sy: Double, ex: Double, ey: Double, d: Double, r: Int, g: Int, b: Int, a: Int = 255) {
        push {
            val dx = ex - sx
            val dy = ey - sy
            translate(sx, sy, 0.0)
            rotate(0.0, 0.0, Math.toDegrees(atan2(dy, dx)))
            fillRender(0.0, -d, sqrt(dx * dx + dy * dy), d * 2, r, g, b, a)
        }
    }
}

abstract class Resource(private val uri: String) {
    enum class ResourceType {
        Images,
        Videos,
        Fonts,
        Scripts,
        Misc
    }

    abstract val resourceType: ResourceType

    private fun loadTester() {
        if (uri.startsWith("jar:")) {
            finishLoad(
                javaClass.getResourceAsStream(
                    "assets/${resourceType.name.lowercase()}/${
                        uri.substring(
                            4
                        )
                    }"
                )!!.readBytes()
            )
            return
        }
        if (uri.startsWith("http:") || uri.startsWith("https:")) {
            finishLoad(URL(uri).readBytes())
            LogManager.getLogger().info("Network resource loaded!")
            return
        }
        val t = File("assets/${resourceType.name.lowercase()}", uri).let {
            if (it.exists()) it.readBytes() else null
        }
        if (t != null) {
            finishLoad(t)
            return
        }
        throw IllegalArgumentException("Not able to load resource: $uri")
    }

    lateinit var thrown: Throwable
        private set
    protected lateinit var bytes: ByteArray
        private set

    fun suspend(): ByteArray {
        loader.join()
        if (::bytes.isInitialized) return bytes
        else throw thrown
    }

    protected open fun afterLoad() {}

    fun getOrNull() = if (::bytes.isInitialized) bytes
    else if (::thrown.isInitialized) throw thrown
    else null

    private val loader = Thread {
        try {
            loadTester()
            LogManager.getLogger().info("Resource $uri loaded.")
        } catch (e: Throwable) {
            thrown = e
        }
    }

    var isLoaded = false
        private set

    private fun finishLoad(b: ByteArray) {
        Minecraft.getInstance().execute {
            bytes = b
            isLoaded = true
            afterLoad()
        }
    }

    protected fun startLoading() {
        loader.start()
    }

    companion object {
        fun clearResource() {
            ImageResource.clearResource()
            FontResource.clearResource()
            Minecraft.getInstance().player?.sendMessage(StringTextComponent("Custom script resource reloaded!!"), Util.DUMMY_UUID)
        }
    }
}

class FontResource(uri: String) : Resource(uri) {
    class STBFontInstance(private val buf: ByteBuffer) : NativeInstance<STBTTFontinfo>() {
        override lateinit var nativeInstance: STBTTFontinfo

        override fun allocateInternal() {
            nativeInstance = STBTTFontinfo.create()
            STBTruetype.stbtt_InitFont(nativeInstance, buf)
        }

        override fun freeInternal() {
            nativeInstance.free()
        }
    }

    companion object {
        private var currentId = 0
        private val resourceMap = mutableMapOf<String, FontResource>()
        fun getOrCreate(uri: String) = resourceMap.getOrPut(uri) { FontResource(uri) }
        fun free(uri: String) = resourceMap[uri]?.info?.free()?.also { resourceMap.remove(uri) }

        fun clearResource() {
            resourceMap.forEach { it.value.info.free() }
            resourceMap.clear()
        }
    }

    private val cid = currentId++

    override val resourceType: ResourceType
        get() = ResourceType.Fonts

    lateinit var info: STBFontInstance
    override fun afterLoad() {
        val byteBuf = BufferUtils.createByteBuffer(bytes.size)
        byteBuf.put(bytes)
        byteBuf.flip()
        info = STBFontInstance(byteBuf)
    }

    init {
        startLoading()
    }

    private var _ascent = createIntArray()
    private var _descent = createIntArray()
    private var _lineGap = createIntArray()

    public val ascent: Int
        get() = this._ascent[0]
    public val descent: Int
        get() = this._descent[0]

    public fun getScale(height: Int): Float {
        info.claim {
            return STBTruetype.stbtt_ScaleForPixelHeight(it, height.toFloat())
        }
    }

    fun calculateWidth(text: String, height: Int): Int {
        info.claim { fontInfo ->
            val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())
            var x = 0
            for (k in text.withIndex()) {
                val ax = createIntArray()
                val lsb = createIntArray()
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, k.value.code, ax, lsb)

                x += (ax[0] * scale).roundToInt()
                x += if (k.index != text.length - 1)
                    (STBTruetype.stbtt_GetCodepointKernAdvance(
                        fontInfo,
                        k.value.code,
                        text[k.index + 1].code
                    ) * scale).roundToInt()
                else (STBTruetype.stbtt_GetCodepointKernAdvance(fontInfo, k.value.code, 0) * scale).roundToInt()
            }

            return x
        }
    }

    fun getBitmap(text: String, height: Int): ByteBuffer {
        info.claim { fontInfo ->
            val width = calculateWidth(text, height)
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, _ascent, _descent, _lineGap)

            val buf = BufferUtils.createByteBuffer(width * height + width)
            val scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, height.toFloat())

            _ascent[0] = (ascent * scale).roundToInt()
            _descent[0] = (descent * scale).roundToInt()

            var x = 0
            for (k in text.withIndex()) {
                val ax = createIntArray()
                val lsb = createIntArray()
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, k.value.code, ax, lsb)

                val c_x1 = createIntArray()
                val c_x2 = createIntArray()
                val c_y1 = createIntArray()
                val c_y2 = createIntArray()

                STBTruetype.stbtt_GetCodepointBitmapBox(fontInfo, k.value.code, scale, scale, c_x1, c_y1, c_x2, c_y2)

                val y = ascent + c_y1[0]
                buf.position(x + (lsb[0] * scale).roundToInt() + (y * width))
                STBTruetype.stbtt_MakeCodepointBitmap(
                    fontInfo,
                    buf,
                    c_x2[0] - c_x1[0],
                    c_y2[0] - c_y1[0],
                    width,
                    scale,
                    scale,
                    k.value.code
                )

                if (k.index != text.length - 1)
                    x += (ax[0] * scale).roundToInt() + (STBTruetype.stbtt_GetCodepointKernAdvance(
                        fontInfo,
                        k.value.code,
                        text[k.index + 1].code
                    ) * scale).roundToInt()
            }
            buf.flip()
            return buf
        }
    }

    private fun createIntArray() = arrayOf(0).toIntArray()
}

class ImageResource(uri: String) : Resource(uri) {
    companion object {
        private var currentId = 0
        fun getNewIdentifier() = ResourceLocation(CustomScript.MOD_ID, "private/images/${currentId++}")
        private val resourceMap = mutableMapOf<String, ImageResource>()
        fun getOrCreate(uri: String) = resourceMap.getOrPut(uri) { ImageResource(uri) }

        fun clearResource() {
            val tm = Minecraft.getInstance().textureManager
            resourceMap.forEach {
                if (it.value::rl.isInitialized && tm.getTexture(it.value.rl) != null)
                    tm.deleteTexture(it.value.rl)
            }
        }
    }

    override val resourceType: ResourceType
        get() = ResourceType.Images
    private lateinit var rl: ResourceLocation

    fun loadAsTexture(): ResourceLocation? {
        if (::rl.isInitialized) return rl
        return getOrNull()?.let {
            val dbuf = ByteBuffer.allocateDirect(it.size)
            dbuf.put(it)
            dbuf.flip()
            rl = getNewIdentifier()

            Minecraft.getInstance().textureManager.loadTexture(rl, DynamicTexture(NativeImage.read(dbuf)))
            rl
        }
    }

    fun bindTexture() {
        loadAsTexture()?.let {
            Minecraft.getInstance().textureManager.bindTexture(it)
        }
    }

    init {
        startLoading()
    }
}