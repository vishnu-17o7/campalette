package com.vishnu.campalette.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.R
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import kotlin.math.max
import kotlin.math.min

object AtelierData {
    fun takePhoto(
        activity: MainActivity,
        cameraExecutor: ExecutorService,
        imageCapture: ImageCapture?,
        onImageCaptured: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        if (imageCapture == null) {
            onError(activity.getString(R.string.capture_unavailable))
            return
        }
        val mainExecutor = ContextCompat.getMainExecutor(activity)
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = imageProxyToBitmap(image)
                        mainExecutor.execute { onImageCaptured(bitmap) }
                    } catch (e: Exception) {
                        Log.d("CameraCapture", "Photo capture failed", e)
                        mainExecutor.execute { onError(activity.getString(R.string.capture_failed)) }
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("CameraCapture", "Photo capture failed", exception)
                    mainExecutor.execute { onError(activity.getString(R.string.capture_failed)) }
                }
            }
        )
    }

    fun extractColorPalette(activity: MainActivity, bitmap: Bitmap): List<PaletteColor> {
        val palette = Palette.from(bitmap).generate()
        val colors = mutableListOf<PaletteColor>()
        fun addColor(nameRes: Int, rgb: Int?) {
            rgb?.let {
                colors.add(paletteColor(activity.getString(nameRes), it))
            }
        }
        addColor(R.string.dominant_color, palette.dominantSwatch?.rgb)
        addColor(R.string.vibrant_color, palette.vibrantSwatch?.rgb)
        addColor(R.string.light_vibrant, palette.lightVibrantSwatch?.rgb)
        addColor(R.string.dark_vibrant, palette.darkVibrantSwatch?.rgb)
        addColor(R.string.muted_color, palette.mutedSwatch?.rgb)
        addColor(R.string.light_muted, palette.lightMutedSwatch?.rgb)
        addColor(R.string.dark_muted, palette.darkMutedSwatch?.rgb)
        return colors
    }

    fun generatePaletteFromSeedColor(activity: MainActivity, seedColor: Int): List<PaletteColor> {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(seedColor, hsv)

        fun shiftHue(degrees: Float): Int {
            val shifted = floatArrayOf((hsv[0] + degrees) % 360f, hsv[1], hsv[2])
            return android.graphics.Color.HSVToColor(shifted)
        }

        fun tint(): Int {
            val tinted = floatArrayOf(hsv[0], (hsv[1] * 0.6f).coerceIn(0f, 1f), (hsv[2] * 1.18f).coerceIn(0f, 1f))
            return android.graphics.Color.HSVToColor(tinted)
        }

        fun shade(): Int {
            val shaded = floatArrayOf(hsv[0], (hsv[1] * 1.05f).coerceIn(0f, 1f), (hsv[2] * 0.6f).coerceIn(0f, 1f))
            return android.graphics.Color.HSVToColor(shaded)
        }

        return listOf(
            activity.getString(R.string.palette_seed) to seedColor,
            activity.getString(R.string.palette_complement) to shiftHue(180f),
            activity.getString(R.string.palette_analog_a) to shiftHue(30f),
            activity.getString(R.string.palette_analog_b) to shiftHue(330f),
            activity.getString(R.string.palette_tint) to tint(),
            activity.getString(R.string.palette_shade) to shade()
        ).map { (name, color) -> paletteColor(name, color) }
    }

    fun sampleColorFromBitmap(
        bitmap: Bitmap,
        touchPoint: androidx.compose.ui.geometry.Offset,
        containerSize: androidx.compose.ui.unit.IntSize
    ): Int? {
        if (containerSize.width == 0 || containerSize.height == 0) return null
        val scale = max(
            containerSize.width.toFloat() / bitmap.width.toFloat(),
            containerSize.height.toFloat() / bitmap.height.toFloat()
        )
        val displayedWidth = bitmap.width * scale
        val displayedHeight = bitmap.height * scale
        val offsetX = (displayedWidth - containerSize.width) / 2f
        val offsetY = (displayedHeight - containerSize.height) / 2f
        val bitmapX = ((touchPoint.x + offsetX) / scale).toInt()
        val bitmapY = ((touchPoint.y + offsetY) / scale).toInt()
        if (bitmapX !in 0 until bitmap.width || bitmapY !in 0 until bitmap.height) return null
        return bitmap.getPixel(bitmapX, bitmapY)
    }

    fun nearestPaletteIndex(targetColor: Int, palette: List<PaletteColor>): Int {
        if (palette.isEmpty()) return -1
        val targetR = android.graphics.Color.red(targetColor)
        val targetG = android.graphics.Color.green(targetColor)
        val targetB = android.graphics.Color.blue(targetColor)
        var bestIndex = 0
        var bestDistance = Int.MAX_VALUE
        palette.forEachIndexed { index, paletteColor ->
            val dr = targetR - paletteColor.red
            val dg = targetG - paletteColor.green
            val db = targetB - paletteColor.blue
            val distance = dr * dr + dg * dg + db * db
            if (distance < bestDistance) {
                bestDistance = distance
                bestIndex = index
            }
        }
        return bestIndex
    }

    fun defaultPalette(): List<PaletteColor> = listOf(
        paletteColor("Deep Moss", 0xFF2A6864.toInt()),
        paletteColor("Paper Tone", 0xFFFDF9F4.toInt()),
        paletteColor("Weathered Leather", 0xFF81542E.toInt()),
        paletteColor("Botanical Study", 0xFF18481A.toInt()),
        paletteColor("Soft Linen", 0xFFE6E2DD.toInt())
    )

    fun buildLibraryPalettes(current: List<PaletteColor>, paletteName: String): List<PaletteStudy> = listOf(
        PaletteStudy(
            name = paletteName,
            colors = current,
            note = "Current working palette",
            capturedAt = timestampLabel(),
            source = "Live Camera"
        ),
        PaletteStudy(
            "Desert Dawn",
            listOf(
                paletteColor("Apricot Dust", 0xFFFEC394.toInt()),
                paletteColor("Leather", 0xFF81542E.toInt()),
                paletteColor("Porcelain", 0xFFFFDCC3.toInt()),
                paletteColor("Paper", 0xFFF4F0EB.toInt()),
                paletteColor("Burnished Bark", 0xFF653D19.toInt())
            ),
            note = "Warm sandstone capture with apricot lift.",
            capturedAt = "Archive",
            source = "Studio Sample"
        ),
        PaletteStudy(
            "Oceanic Depth",
            listOf(
                paletteColor("Ink", 0xFF00201E.toInt()),
                paletteColor("Glass Lake", 0xFF06504C.toInt()),
                paletteColor("Sea Foam", 0xFF95D2CC.toInt()),
                paletteColor("Harbor", 0xFF1F5F5B.toInt()),
                paletteColor("Mist", 0xFFB0EEE8.toInt())
            ),
            note = "Harbor-toned complementary study.",
            capturedAt = "Archive",
            source = "Studio Sample"
        ),
        PaletteStudy(
            "Vintage Linen",
            listOf(
                paletteColor("Warm White", 0xFFFDF9F4.toInt()),
                paletteColor("Pressed Paper", 0xFFEBE8E3.toInt()),
                paletteColor("Gesso", 0xFFF1EDE8.toInt()),
                paletteColor("Archive", 0xFFDDD9D5.toInt()),
                paletteColor("Soft Greige", 0xFFE6E2DD.toInt())
            ),
            note = "Paper-tone neutral set.",
            capturedAt = "Archive",
            source = "Studio Sample"
        )
    )

    fun createCapturedStudy(
        name: String,
        colors: List<PaletteColor>,
        source: String = "Live Camera"
    ): PaletteStudy = PaletteStudy(
        name = name,
        colors = colors,
        note = "Captured from the current camera study.",
        capturedAt = timestampLabel(),
        source = source
    )

    fun harmonyPalette(activity: MainActivity, seedColor: Int, mode: HarmonyMode): List<PaletteColor> {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(seedColor, hsv)

        fun shift(degrees: Float, satFactor: Float = 1f, valueFactor: Float = 1f): Int {
            val shifted = floatArrayOf(
                (hsv[0] + degrees + 360f) % 360f,
                (hsv[1] * satFactor).coerceIn(0f, 1f),
                (hsv[2] * valueFactor).coerceIn(0f, 1f)
            )
            return android.graphics.Color.HSVToColor(shifted)
        }

        val set = when (mode) {
            HarmonyMode.Analogous -> listOf(
                activity.getString(R.string.palette_seed) to seedColor,
                activity.getString(R.string.palette_seed_left) to shift(-25f),
                activity.getString(R.string.palette_seed_right) to shift(25f),
                activity.getString(R.string.palette_tint) to shift(0f, satFactor = 0.6f, valueFactor = 1.18f),
                activity.getString(R.string.palette_shade) to shift(0f, satFactor = 1.05f, valueFactor = 0.62f)
            )
            HarmonyMode.Complementary -> listOf(
                activity.getString(R.string.palette_seed) to seedColor,
                activity.getString(R.string.palette_complement) to shift(180f),
                activity.getString(R.string.palette_split_a) to shift(150f),
                activity.getString(R.string.palette_split_b) to shift(210f),
                activity.getString(R.string.palette_bridge) to shift(0f, satFactor = 0.65f, valueFactor = 1.1f)
            )
            HarmonyMode.Tonal -> listOf(
                activity.getString(R.string.palette_seed) to seedColor,
                activity.getString(R.string.palette_tint_1) to shift(0f, satFactor = 0.72f, valueFactor = 1.15f),
                activity.getString(R.string.palette_tint_2) to shift(0f, satFactor = 0.48f, valueFactor = 1.24f),
                activity.getString(R.string.palette_shade_1) to shift(0f, satFactor = 1.06f, valueFactor = 0.72f),
                activity.getString(R.string.palette_shade_2) to shift(0f, satFactor = 1.1f, valueFactor = 0.48f)
            )
        }
        return set.map { (label, color) -> paletteColor(label, color) }
    }

    fun timestampLabel(): String =
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())

    fun paletteColor(name: String, color: Int): PaletteColor = PaletteColor(
        name = name,
        color = color,
        hexCode = color.toHexCode(),
        red = android.graphics.Color.red(color),
        green = android.graphics.Color.green(color),
        blue = android.graphics.Color.blue(color)
    )

    fun rgbToCmyk(red: Int, green: Int, blue: Int): FloatArray {
        val r = red / 255f
        val g = green / 255f
        val b = blue / 255f
        val k = 1f - max(r, max(g, b))
        if (k >= 1f) return floatArrayOf(0f, 0f, 0f, 100f)
        val c = (1f - r - k) / (1f - k)
        val m = (1f - g - k) / (1f - k)
        val y = (1f - b - k) / (1f - k)
        return floatArrayOf(c * 100f, m * 100f, y * 100f, k * 100f)
    }

    fun Float.format0(): String = String.format("%.0f", this)

    fun downscaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val ratio = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val newWidth = (width * ratio).toInt().coerceAtLeast(1)
        val newHeight = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun encodeStudyJson(studies: List<PaletteStudy>): String {
        val sb = StringBuilder("[")
        studies.forEachIndexed { i, s ->
            if (i > 0) sb.append(",")
            sb.append("""{"n":"${s.name}","c":"${s.capturedAt}","s":"${s.source}","cl":[""")
            s.colors.forEachIndexed { j, c ->
                if (j > 0) sb.append(",")
                sb.append("""{"nm":"${c.name}","h":${c.color},"hx":"${c.hexCode}","r":${c.red},"g":${c.green},"b":${c.blue}}""")
            }
            sb.append("]}")
        }
        sb.append("]")
        return sb.toString()
    }

    fun decodeStudyJson(json: String): List<PaletteStudy> {
        if (json.isBlank() || json == "[]" || json == "null") return emptyList()
        val studies = mutableListOf<PaletteStudy>()
        try {
            val blockRegex = Regex("""\{"n":"([^"]+)","c":"([^"]+)","s":"([^"]+)","cl":\[(.*?)\]\}""")
            blockRegex.findAll(json).forEach { match ->
                val name = match.groupValues[1]
                val capturedAt = match.groupValues[2]
                val source = match.groupValues[3]
                val colorsRaw = match.groupValues[4]
                val colors = mutableListOf<PaletteColor>()
                val colorRegex = Regex("""\{"nm":"([^"]+)","h":(-?\d+),"hx":"([^"]+)","r":(\d+),"g":(\d+),"b":(\d+)\}""")
                colorRegex.findAll(colorsRaw).forEach { cm ->
                    colors.add(PaletteColor(
                        name = cm.groupValues[1],
                        color = cm.groupValues[2].toInt(),
                        hexCode = cm.groupValues[3],
                        red = cm.groupValues[4].toInt(),
                        green = cm.groupValues[5].toInt(),
                        blue = cm.groupValues[6].toInt()
                    ))
                }
                studies.add(PaletteStudy(name = name, colors = colors, capturedAt = capturedAt, source = source))
            }
        } catch (_: Exception) { }
        return studies
    }

    private fun Int.toHexCode(): String = String.format("#%06X", 0xFFFFFF and this)

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        if (image.format == ImageFormat.JPEG || image.planes.size == 1) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Bitmap decode failed")
        }

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val decoded = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
            ?: throw IllegalStateException("Bitmap decode failed")

        return if (image.imageInfo.rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        } else {
            decoded
        }
    }
}

enum class HarmonyMode(val label: String) {
    Analogous("Analogous Study"),
    Complementary("Complementary Study"),
    Tonal("Tonal Study")
}
