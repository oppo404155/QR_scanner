package com.example.qrscanner

import android.graphics.ImageFormat
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import java.nio.ByteBuffer

class ImageAnalyzer(
    private val onQrcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @RequiresApi(Build.VERSION_CODES.M)
    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    @RequiresApi(Build.VERSION_CODES.M)
    override fun analyze(image: ImageProxy) {
        if (image.format in supportedImageFormats) {
            val bytes = image.planes.first().buffer.toByteArr()
            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = MultiFormatReader().apply {
                    setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)))
                }.decode(binaryBitmap)
                onQrcodeScanned(result.text.toString())


            } catch (exception: Exception) {
                exception.printStackTrace()

            }
            finally {
                image.close()
            }

        }

    }

    private fun ByteBuffer.toByteArr(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}