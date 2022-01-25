package com.github.aui.clion.aui

import com.intellij.ui.scale.JBUIScale
import java.awt.Graphics
import java.awt.Image
import java.awt.image.ColorModel
import java.awt.image.DirectColorModel
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

    external fun nInit()
    external fun nSetSize(width: Int, height: Int)
    external fun nRender(scale: Float, mImageBuffer: IntArray?)

    init {
        nInit()
    }

    override fun paint(g: Graphics?) {

        super.paint(g)
        val scale = JBUIScale.sysScale(this)
        val w = (width * scale).toInt()
        val h = (height * scale).toInt()
        if (mPrevWidth != w || mPrevHeight != h) {
            nSetSize(w, h)
            mPrevWidth = w
            mPrevHeight = h
            mImageBuffer = IntArray(w * h)
            mImageSource = MemoryImageSource(w, h, ColorModel.getRGBdefault(), mImageBuffer, 0, w);
            mImage = createImage(mImageSource)
        }
        nRender(scale, mImageBuffer)
        g!!.drawImage(mImage, 0, 0, width, height, this)
    }

}