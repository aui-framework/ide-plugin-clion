package com.github.aui.clion.aui

import com.intellij.ui.scale.JBUIScale
import java.awt.Graphics
import java.awt.Image
import java.awt.image.ColorModel
import java.awt.image.MemoryImageSource
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Paths
import javax.swing.JPanel

open class AUIViewContainer: JPanel() {
    private var mNativePtr: Long = 0
    private var mPrevWidth = 0
    private var mPrevHeight = 0
    private lateinit var mImageBuffer: IntArray
    private lateinit var mImageSource: MemoryImageSource
    private lateinit var mImage: Image

    companion object {
        init {
            val osArch = System.getProperty("os.arch")!!
            val name = System.mapLibraryName("aui_clion-$osArch")
            val tempFilePath = Paths.get(System.getProperty("java.io.tmpdir"), name)

            try {
                AUIViewContainer::class.java.getResourceAsStream("/NATIVES/$name").use { input ->
                    FileOutputStream(tempFilePath.toString()).use { output ->
                        input!!.copyTo(output)
                    }
                }
            } catch (_: IOException) {}

            System.load(tempFilePath.toString())
        }
    }

    private external fun nInit()
    private external fun nSetSize(width: Int, height: Int)
    private external fun nRender(scale: Float, mImageBuffer: IntArray?): Boolean

    init {
        nInit()
    }

    override fun paint(g: Graphics?) {

        super.paint(g)
        val scale = JBUIScale.sysScale(this)
        val w = (width * scale).toInt()
        val h = (height * scale).toInt()
        var updateImage = false
        if (mPrevWidth != w || mPrevHeight != h) {
            nSetSize(w, h)
            mPrevWidth = w
            mPrevHeight = h
            mImageBuffer = IntArray(w * h)
            updateImage = true
        }
        if (nRender(scale, mImageBuffer)) updateImage = true
        if (updateImage) {
            mImageSource = MemoryImageSource(w, h, ColorModel.getRGBdefault(), mImageBuffer, 0, w);
            mImageSource.setFullBufferUpdates(true)
            mImage = createImage(mImageSource)
        }
        g!!.drawImage(mImage, 0, 0, width, height, this)
    }

}