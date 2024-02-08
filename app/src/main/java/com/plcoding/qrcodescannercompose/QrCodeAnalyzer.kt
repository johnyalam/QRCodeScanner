package com.plcoding.qrcodescannercompose

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.* // ktlint-disable no-wildcard-imports
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888,
    )

    override fun analyze(image: ImageProxy) {
        if (image.format in supportedImageFormats) {
            val bytes = image.planes.first().buffer.toByteArray()
            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false,
            )
            val binaryBmp = BinaryBitmap(HybridBinarizer(source))
            try {
                val hints: MutableMap<DecodeHintType, Any> = HashMap()
                hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(
                    BarcodeFormat.QR_CODE,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                )

                val result = MultiFormatReader().apply {
                    setHints(
                        hints,
                    )
                }.decode(binaryBmp)

                onQrCodeScanned(result.text)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}
