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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import androidx.palette.graphics.Palette
import com.vishnu.campalette.BuildConfig
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.R
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

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
                        val bitmap = image.use { imageProxyToBitmap(it) }
                        mainExecutor.execute { onImageCaptured(bitmap) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) {
                            Log.d("CameraCapture", "Photo capture failed", e)
                        }
                        mainExecutor.execute { onError(activity.getString(R.string.capture_failed)) }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    if (BuildConfig.DEBUG) {
                        Log.d("CameraCapture", "Photo capture failed", exception)
                    }
                    mainExecutor.execute { onError(activity.getString(R.string.capture_failed)) }
                }
            }
        )
    }

    const val MIN_PALETTE_COLOR_COUNT = 2
    const val MAX_PALETTE_COLOR_COUNT = 10
    const val DEFAULT_PALETTE_COLOR_COUNT = 7
    private const val PALETTE_EXTRACT_MAX_DIMENSION = 256

    /**
     * Extracts up to [colorCount] colors from [bitmap].
     *
     * The classic named swatches (dominant, vibrant, muted, ...) are used first, in
     * priority order, since they read well in the UI. If the caller asks for more
     * colors than those seven targets provide - or a photo is missing some of them -
     * remaining slots are filled from the next most populous distinct swatches.
     */
    suspend fun extractColorPalette(
        activity: MainActivity,
        bitmap: Bitmap,
        colorCount: Int = DEFAULT_PALETTE_COLOR_COUNT
    ): List<PaletteColor> = withContext(Dispatchers.Default) {
        val targetCount = colorCount.coerceIn(MIN_PALETTE_COLOR_COUNT, MAX_PALETTE_COLOR_COUNT)
        // Palette.Builder still copies/scales its input; shrink large photos first.
        val source = downscaleBitmap(bitmap, PALETTE_EXTRACT_MAX_DIMENSION)
        try {
            val palette = Palette.Builder(source)
                .maximumColorCount(max(targetCount * 4, 16))
                .generate()
            val colors = mutableListOf<PaletteColor>()
            val usedRgb = mutableSetOf<Int>()

            fun addColor(nameRes: Int, rgb: Int?) {
                if (colors.size >= targetCount) return
                val value = rgb ?: return
                if (!usedRgb.add(value)) return
                colors.add(paletteColor(activity.getString(nameRes), value))
            }

            addColor(R.string.dominant_color, palette.dominantSwatch?.rgb)
            addColor(R.string.vibrant_color, palette.vibrantSwatch?.rgb)
            addColor(R.string.muted_color, palette.mutedSwatch?.rgb)
            addColor(R.string.light_vibrant, palette.lightVibrantSwatch?.rgb)
            addColor(R.string.dark_vibrant, palette.darkVibrantSwatch?.rgb)
            addColor(R.string.light_muted, palette.lightMutedSwatch?.rgb)
            addColor(R.string.dark_muted, palette.darkMutedSwatch?.rgb)

            if (colors.size < targetCount) {
                val extraSwatches = palette.swatches
                    .sortedByDescending { it.population }
                    .filter { usedRgb.add(it.rgb) }
                for (swatch in extraSwatches) {
                    if (colors.size >= targetCount) break
                    val label = activity.getString(R.string.additional_color, colors.size + 1)
                    colors.add(paletteColor(label, swatch.rgb))
                }
            }

            colors
        } finally {
            if (source !== bitmap) source.recycle()
        }
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
        return bitmap[bitmapX, bitmapY]
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

    fun Float.format0(): String = String.format(Locale.getDefault(), "%.0f", this)

    fun downscaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val ratio = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
        val newWidth = (width * ratio).toInt().coerceAtLeast(1)
        val newHeight = (height * ratio).toInt().coerceAtLeast(1)
        return bitmap.scale(newWidth, newHeight)
    }

    fun encodeStudyJson(studies: List<PaletteStudy>): String {
        return JSONArray().apply {
            studies.forEach { study ->
                put(JSONObject().apply {
                    put("n", study.name)
                    put("nt", study.note)
                    put("c", study.capturedAt)
                    put("s", study.source)
                    put("cl", JSONArray().apply {
                        study.colors.forEach { color ->
                            put(JSONObject().apply {
                                put("nm", color.name)
                                put("h", color.color)
                                put("hx", color.hexCode)
                                put("r", color.red)
                                put("g", color.green)
                                put("b", color.blue)
                            })
                        }
                    })
                })
            }
        }.toString()
    }

    fun decodeStudyJson(json: String): List<PaletteStudy> {
        if (json.isBlank() || json == "[]" || json == "null") return emptyList()
        return try {
            val array = JSONArray(json)
            buildList {
                for (studyIndex in 0 until array.length()) {
                    val stored = array.getJSONObject(studyIndex)
                    val storedColors = stored.optJSONArray("cl") ?: JSONArray()
                    val colors = buildList {
                        for (colorIndex in 0 until storedColors.length()) {
                            val color = storedColors.getJSONObject(colorIndex)
                            add(PaletteColor(
                                name = color.optString("nm"),
                                color = color.optInt("h"),
                                hexCode = color.optString("hx"),
                                red = color.optInt("r"),
                                green = color.optInt("g"),
                                blue = color.optInt("b")
                            ))
                        }
                    }
                    add(PaletteStudy(
                        name = stored.optString("n"),
                        colors = colors,
                        note = stored.optString("nt"),
                        capturedAt = stored.optString("c"),
                        source = stored.optString("s")
                    ))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            decodeLegacyStudyJson(json)
        }
    }

    private fun decodeLegacyStudyJson(json: String): List<PaletteStudy> {
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
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
        return studies
    }

    private fun Int.toHexCode(): String = String.format("#%06X", 0xFFFFFF and this)

    fun rgbToHsl(red: Int, green: Int, blue: Int): FloatArray {
        val r = red / 255f
        val g = green / 255f
        val b = blue / 255f
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val l = (max + min) / 2f
        if (max == min) return floatArrayOf(0f, 0f, l * 100f)
        val d = max - min
        val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        val h = when (max) {
            r -> ((g - b) / d + (if (g < b) 6 else 0)) % 6
            g -> (b - r) / d + 2
            else -> (r - g) / d + 4
        } * 60f
        return floatArrayOf(h, s * 100f, l * 100f)
    }

    fun hslToRgb(h: Float, s: Float, l: Float): Int {
        val hf = h / 360f
        val sf = s / 100f
        val lf = l / 100f
        if (sf == 0f) {
            val v = (lf * 255).toInt()
            return android.graphics.Color.rgb(v, v, v)
        }
        val q = if (lf < 0.5f) lf * (1 + sf) else lf + sf - lf * sf
        val p = 2 * lf - q
        fun hue2rgb(p: Float, q: Float, t: Float): Float {
            var tt = t
            if (tt < 0) tt += 1f
            if (tt > 1) tt -= 1f
            return when {
                tt < 1f / 6f -> p + (q - p) * 6f * tt
                tt < 1f / 2f -> q
                tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
                else -> p
            }
        }
        val r = (hue2rgb(p, q, hf + 1f / 3f) * 255).toInt()
        val g = (hue2rgb(p, q, hf) * 255).toInt()
        val b = (hue2rgb(p, q, hf - 1f / 3f) * 255).toInt()
        return android.graphics.Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    fun generateRelatedColors(seedColor: Int): Triple<List<PaletteColor>, List<PaletteColor>, List<PaletteColor>> {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(seedColor, hsv)
        val tints = (1..4).map { i ->
            val t = i / 5f
            val newV = hsv[2] + (1f - hsv[2]) * t
            val newS = hsv[1] * (1f - t * 0.5f)
            paletteColor("Tint $i", android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], newS.coerceIn(0f, 1f), newV.coerceIn(0f, 1f))))
        }
        val shades = (1..4).map { i ->
            val t = i / 5f
            val newV = hsv[2] * (1f - t)
            val newS = (hsv[1] * (1f + t * 0.3f)).coerceIn(0f, 1f)
            paletteColor("Shade $i", android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], newS, newV.coerceIn(0f, 1f))))
        }
        val tones = (1..4).map { i ->
            val t = i / 5f
            val newS = hsv[1] * (1f - t * 0.6f)
            val newV = hsv[2] + (0.5f - hsv[2]) * t
            paletteColor("Tone $i", android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], newS.coerceIn(0f, 1f), newV.coerceIn(0f, 1f))))
        }
        return Triple(tints, shades, tones)
    }

    private val namedColors = listOf(
        "Maroon" to 0xFF800000.toInt(),
        "Dark Red" to 0xFF8B0000.toInt(),
        "Brown" to 0xFFA52A2A.toInt(),
        "Firebrick" to 0xFFB22222.toInt(),
        "Crimson" to 0xFFDC143C.toInt(),
        "Red" to 0xFFFF0000.toInt(),
        "Tomato" to 0xFFFF6347.toInt(),
        "Coral" to 0xFFFF7F50.toInt(),
        "Indian Red" to 0xFFCD5C5C.toInt(),
        "Light Coral" to 0xFFF08080.toInt(),
        "Dark Salmon" to 0xFFE9967A.toInt(),
        "Salmon" to 0xFFFA8072.toInt(),
        "Light Salmon" to 0xFFFFA07A.toInt(),
        "Orange Red" to 0xFFFF4500.toInt(),
        "Dark Orange" to 0xFFFF8C00.toInt(),
        "Orange" to 0xFFFFA500.toInt(),
        "Gold" to 0xFFFFD700.toInt(),
        "Dark Golden Rod" to 0xFFB8860B.toInt(),
        "Golden Rod" to 0xFFDAA520.toInt(),
        "Pale Golden Rod" to 0xFFEEE8AA.toInt(),
        "Dark Khaki" to 0xFFBDB76B.toInt(),
        "Khaki" to 0xFFF0E68C.toInt(),
        "Olive" to 0xFF808000.toInt(),
        "Yellow" to 0xFFFFFF00.toInt(),
        "Yellow Green" to 0xFF9ACD32.toInt(),
        "Dark Olive Green" to 0xFF556B2F.toInt(),
        "Olive Drab" to 0xFF6B8E23.toInt(),
        "Lawn Green" to 0xFF7CFC00.toInt(),
        "Chartreuse" to 0xFF7FFF00.toInt(),
        "Green Yellow" to 0xFFADFF2F.toInt(),
        "Dark Green" to 0xFF006400.toInt(),
        "Green" to 0xFF008000.toInt(),
        "Forest Green" to 0xFF228B22.toInt(),
        "Lime" to 0xFF00FF00.toInt(),
        "Lime Green" to 0xFF32CD32.toInt(),
        "Spring Green" to 0xFF00FF7F.toInt(),
        "Sea Green" to 0xFF2E8B57.toInt(),
        "Medium Sea Green" to 0xFF3CB371.toInt(),
        "Light Sea Green" to 0xFF20B2AA.toInt(),
        "Dark Cyan" to 0xFF008B8B.toInt(),
        "Teal" to 0xFF008080.toInt(),
        "Aqua" to 0xFF00FFFF.toInt(),
        "Dark Turquoise" to 0xFF00CED1.toInt(),
        "Turquoise" to 0xFF40E0D0.toInt(),
        "Medium Turquoise" to 0xFF48D1CC.toInt(),
        "Cadet Blue" to 0xFF5F9EA0.toInt(),
        "Steel Blue" to 0xFF4682B4.toInt(),
        "Light Steel Blue" to 0xFFB0C4DE.toInt(),
        "Powder Blue" to 0xFFB0E0E6.toInt(),
        "Light Blue" to 0xFFADD8E6.toInt(),
        "Sky Blue" to 0xFF87CEEB.toInt(),
        "Deep Sky Blue" to 0xFF00BFFF.toInt(),
        "Dodger Blue" to 0xFF1E90FF.toInt(),
        "Cornflower Blue" to 0xFF6495ED.toInt(),
        "Royal Blue" to 0xFF4169E1.toInt(),
        "Blue" to 0xFF0000FF.toInt(),
        "Medium Blue" to 0xFF0000CD.toInt(),
        "Dark Blue" to 0xFF00008B.toInt(),
        "Navy" to 0xFF000080.toInt(),
        "Midnight Blue" to 0xFF191970.toInt(),
        "Lavender" to 0xFFE6E6FA.toInt(),
        "Thistle" to 0xFFD8BFD8.toInt(),
        "Plum" to 0xFFDDA0DD.toInt(),
        "Violet" to 0xFFEE82EE.toInt(),
        "Orchid" to 0xFFDA70D6.toInt(),
        "Magenta" to 0xFFFF00FF.toInt(),
        "Medium Orchid" to 0xFFBA55D3.toInt(),
        "Medium Purple" to 0xFF9370DB.toInt(),
        "Blue Violet" to 0xFF8A2BE2.toInt(),
        "Dark Violet" to 0xFF9400D3.toInt(),
        "Dark Orchid" to 0xFF9932CC.toInt(),
        "Dark Magenta" to 0xFF8B008B.toInt(),
        "Purple" to 0xFF800080.toInt(),
        "Indigo" to 0xFF4B0082.toInt(),
        "Slate Blue" to 0xFF6A5ACD.toInt(),
        "Dark Slate Blue" to 0xFF483D8B.toInt(),
        "Rebecca Purple" to 0xFF663399.toInt(),
        "Pink" to 0xFFFFC0CB.toInt(),
        "Light Pink" to 0xFFFFB6C1.toInt(),
        "Hot Pink" to 0xFFFF69B4.toInt(),
        "Deep Pink" to 0xFFFF1493.toInt(),
        "Pale Violet Red" to 0xFFDB7093.toInt(),
        "Medium Violet Red" to 0xFFC71585.toInt(),
        "Rosy Brown" to 0xFFBC8F8F.toInt(),
        "Sandy Brown" to 0xFFF4A460.toInt(),
        "Goldenrod" to 0xFFDAA520.toInt(),
        "Peru" to 0xFFCD853F.toInt(),
        "Chocolate" to 0xFFD2691E.toInt(),
        "Saddle Brown" to 0xFF8B4513.toInt(),
        "Sienna" to 0xFFA0522D.toInt(),
        "Burlywood" to 0xFFDEB887.toInt(),
        "Tan" to 0xFFD2B48C.toInt(),
        "Wheat" to 0xFFF5DEB3.toInt(),
        "Navajo White" to 0xFFFFDEAD.toInt(),
        "Bisque" to 0xFFFFE4C4.toInt(),
        "Blanched Almond" to 0xFFFFEBCD.toInt(),
        "Antique White" to 0xFFFAEBD7.toInt(),
        "Linen" to 0xFFFAF0E6.toInt(),
        "Old Lace" to 0xFFFDF5E6.toInt(),
        "Floral White" to 0xFFFFFAF0.toInt(),
        "Ivory" to 0xFFFFFFF0.toInt(),
        "Honeydew" to 0xFFF0FFF0.toInt(),
        "Mint Cream" to 0xFFF5FFFA.toInt(),
        "Azure" to 0xFFF0FFFF.toInt(),
        "Alice Blue" to 0xFFF0F8FF.toInt(),
        "Ghost White" to 0xFFF8F8FF.toInt(),
        "White Smoke" to 0xFFF5F5F5.toInt(),
        "Seashell" to 0xFFFFF5EE.toInt(),
        "Beige" to 0xFFF5F5DC.toInt(),
        "Cornsilk" to 0xFFFFF8DC.toInt(),
        "Lemon Chiffon" to 0xFFFFFACD.toInt(),
        "Papaya Whip" to 0xFFFFEFD5.toInt(),
        "Peach Puff" to 0xFFFFDAB9.toInt(),
        "Moccasin" to 0xFFFFE4B5.toInt(),
        "Pale Goldenrod" to 0xFFEEE8AA.toInt(),
        "Misty Rose" to 0xFFFFE4E1.toInt(),
        "Lavender Blush" to 0xFFFFF0F5.toInt(),
        "Snow" to 0xFFFFFAFA.toInt(),
        "White" to 0xFFFFFFFF.toInt(),
        "Black" to 0xFF000000.toInt(),
        "Dark Slate Gray" to 0xFF2F4F4F.toInt(),
        "Dim Gray" to 0xFF696969.toInt(),
        "Slate Gray" to 0xFF708090.toInt(),
        "Light Slate Gray" to 0xFF778899.toInt(),
        "Gray" to 0xFF808080.toInt(),
        "Dark Gray" to 0xFFA9A9A9.toInt(),
        "Silver" to 0xFFC0C0C0.toInt(),
        "Light Gray" to 0xFFD3D3D3.toInt(),
        "Gainsboro" to 0xFFDCDCDC.toInt(),
        "Crimson" to 0xFFDC143C.toInt(),
        "Cyan" to 0xFF00FFFF.toInt(),
        "Aquamarine" to 0xFF7FFFD4.toInt(),
        "Medium Aquamarine" to 0xFF66CDAA.toInt(),
        "Pale Green" to 0xFF98FB98.toInt(),
        "Light Green" to 0xFF90EE90.toInt(),
        "Dark Sea Green" to 0xFF8FBC8F.toInt(),
        "Sea Green" to 0xFF2E8B57.toInt(),
        "Rosy Brown" to 0xFFBC8F8F.toInt(),
        "Sandy Brown" to 0xFFF4A460.toInt(),
        "Peru" to 0xFFCD853F.toInt(),
        "Chocolate" to 0xFFD2691E.toInt(),
        "Saddle Brown" to 0xFF8B4513.toInt(),
        "Sienna" to 0xFFA0522D.toInt(),
        "Burlywood" to 0xFFDEB887.toInt(),
        "Tan" to 0xFFD2B48C.toInt(),
        "Wheat" to 0xFFF5DEB3.toInt(),
        "Navajo White" to 0xFFFFDEAD.toInt(),
        "Bisque" to 0xFFFFE4C4.toInt(),
        "Blanched Almond" to 0xFFFFEBCD.toInt(),
        "Antique White" to 0xFFFAEBD7.toInt(),
        "Linen" to 0xFFFAF0E6.toInt(),
        "Old Lace" to 0xFFFDF5E6.toInt(),
        "Floral White" to 0xFFFFFAF0.toInt(),
        "Ivory" to 0xFFFFFFF0.toInt(),
        "Honeydew" to 0xFFF0FFF0.toInt(),
        "Mint Cream" to 0xFFF5FFFA.toInt(),
        "Azure" to 0xFFF0FFFF.toInt(),
        "Alice Blue" to 0xFFF0F8FF.toInt(),
        "Ghost White" to 0xFFF8F8FF.toInt(),
        "White Smoke" to 0xFFF5F5F5.toInt(),
        "Seashell" to 0xFFFFF5EE.toInt(),
        "Beige" to 0xFFF5F5DC.toInt(),
        "Cornsilk" to 0xFFFFF8DC.toInt(),
        "Lemon Chiffon" to 0xFFFFFACD.toInt(),
        "Papaya Whip" to 0xFFFFEFD5.toInt(),
        "Peach Puff" to 0xFFFFDAB9.toInt(),
        "Moccasin" to 0xFFFFE4B5.toInt(),
        "Pale Goldenrod" to 0xFFEEE8AA.toInt(),
        "Misty Rose" to 0xFFFFE4E1.toInt(),
        "Lavender Blush" to 0xFFFFF0F5.toInt(),
        "Snow" to 0xFFFFFAFA.toInt(),
        "White" to 0xFFFFFFFF.toInt(),
        "Black" to 0xFF000000.toInt(),
        "Dark Slate Gray" to 0xFF2F4F4F.toInt(),
        "Dim Gray" to 0xFF696969.toInt(),
        "Slate Gray" to 0xFF708090.toInt(),
        "Light Slate Gray" to 0xFF778899.toInt(),
        "Gray" to 0xFF808080.toInt(),
        "Dark Gray" to 0xFFA9A9A9.toInt(),
        "Silver" to 0xFFC0C0C0.toInt(),
        "Light Gray" to 0xFFD3D3D3.toInt(),
        "Gainsboro" to 0xFFDCDCDC.toInt()
    )

    fun guessColorName(color: Int): String {
        val r = android.graphics.Color.red(color)
        val g = android.graphics.Color.green(color)
        val b = android.graphics.Color.blue(color)
        var bestName = "Custom"
        var bestDist = Int.MAX_VALUE
        namedColors.forEach { (name, rgb) ->
            val nr = android.graphics.Color.red(rgb)
            val ng = android.graphics.Color.green(rgb)
            val nb = android.graphics.Color.blue(rgb)
            val dist = (r - nr) * (r - nr) + (g - ng) * (g - ng) + (b - nb) * (b - nb)
            if (dist < bestDist) {
                bestDist = dist
                bestName = name
            }
        }
        return bestName
    }

    data class PaletteStats(
        val avgSaturation: Float,
        val warmth: String,
        val contrastRatio: Float,
        val lightestHex: String,
        val darkestHex: String
    )

    fun paletteStats(palette: List<PaletteColor>): PaletteStats {
        if (palette.isEmpty()) return PaletteStats(0f, "Neutral", 1f, "#000000", "#FFFFFF")
        var totalSat = 0f
        var hueSin = 0.0
        var hueCos = 0.0
        var lightest = palette.first()
        var darkest = palette.first()
        palette.forEach { pc ->
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(pc.color, hsv)
            totalSat += hsv[1]
            val hueRadians = hsv[0] * PI / 180.0
            hueSin += sin(hueRadians)
            hueCos += cos(hueRadians)
            val lum = android.graphics.Color.red(pc.color) * 0.299f + android.graphics.Color.green(pc.color) * 0.587f + android.graphics.Color.blue(pc.color) * 0.114f
            val lightLum = android.graphics.Color.red(lightest.color) * 0.299f + android.graphics.Color.green(lightest.color) * 0.587f + android.graphics.Color.blue(lightest.color) * 0.114f
            val darkLum = android.graphics.Color.red(darkest.color) * 0.299f + android.graphics.Color.green(darkest.color) * 0.587f + android.graphics.Color.blue(darkest.color) * 0.114f
            if (lum > lightLum) lightest = pc
            if (lum < darkLum) darkest = pc
        }
        val avgSat = totalSat / palette.size
        val avgHue = ((atan2(hueSin, hueCos) * 180.0 / PI + 360.0) % 360.0).toFloat()
        val warmth = when {
            avgHue in 0f..60f || avgHue >= 300f -> "Warm"
            avgHue in 120f..180f -> "Cool"
            else -> "Neutral"
        }
        val l1 = android.graphics.Color.luminance(lightest.color).toFloat() + 0.05f
        val l2 = android.graphics.Color.luminance(darkest.color).toFloat() + 0.05f
        val contrast = max(l1, l2) / min(l1, l2)
        return PaletteStats(avgSat * 100f, warmth, contrast, lightest.hexCode, darkest.hexCode)
    }

    fun exportCss(palette: List<PaletteColor>): String {
        return palette.mapIndexed { i, c ->
            "  --color-${i + 1}: ${c.hexCode}; /* ${c.name} */"
        }.joinToString("\n", "/* Campalette CSS Export */\n:root {\n", "\n}")
    }

    fun exportSwift(palette: List<PaletteColor>): String {
        return palette.mapIndexed { i, c ->
            "let color${i + 1} = UIColor(red: ${c.red / 255f}, green: ${c.green / 255f}, blue: ${c.blue / 255f}, alpha: 1.0) // ${c.name}"
        }.joinToString("\n", "// Campalette Swift Export\n", "\n")
    }

    fun exportAndroidRes(palette: List<PaletteColor>): String {
        return palette.mapIndexed { i, c ->
            "    <color name=\"palette_color_${i + 1}\">${c.hexCode}</color> <!-- ${c.name} -->"
        }.joinToString("\n", "<!-- Campalette Android Export -->\n<resources>\n", "\n</resources>")
    }

    fun exportFigma(palette: List<PaletteColor>): String {
        return JSONArray().apply {
            palette.forEach { color ->
                put(JSONObject().apply {
                    put("name", color.name)
                    put("color", JSONObject().apply {
                        put("r", color.red / 255f)
                        put("g", color.green / 255f)
                        put("b", color.blue / 255f)
                        put("a", 1f)
                    })
                })
            }
        }.toString(2)
    }

    enum class ColorBlindnessType { Protanopia, Deuteranopia, Tritanopia }

    private val protanopiaMatrix = floatArrayOf(
        0.567f, 0.433f, 0f, 0f, 0f,
        0.558f, 0.442f, 0f, 0f, 0f,
        0f, 0.242f, 0.758f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    private val deuteranopiaMatrix = floatArrayOf(
        0.625f, 0.375f, 0f, 0f, 0f,
        0.7f, 0.3f, 0f, 0f, 0f,
        0f, 0.3f, 0.7f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    private val tritanopiaMatrix = floatArrayOf(
        0.95f, 0.05f, 0f, 0f, 0f,
        0f, 0.433f, 0.567f, 0f, 0f,
        0f, 0.475f, 0.525f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    fun applyColorBlindness(color: Int, type: ColorBlindnessType): Int {
        val matrix = when (type) {
            ColorBlindnessType.Protanopia -> protanopiaMatrix
            ColorBlindnessType.Deuteranopia -> deuteranopiaMatrix
            ColorBlindnessType.Tritanopia -> tritanopiaMatrix
        }
        val r = android.graphics.Color.red(color)
        val g = android.graphics.Color.green(color)
        val b = android.graphics.Color.blue(color)
        val newR = (matrix[0] * r + matrix[1] * g + matrix[2] * b).toInt().coerceIn(0, 255)
        val newG = (matrix[5] * r + matrix[6] * g + matrix[7] * b).toInt().coerceIn(0, 255)
        val newB = (matrix[10] * r + matrix[11] * g + matrix[12] * b).toInt().coerceIn(0, 255)
        return android.graphics.Color.rgb(newR, newG, newB)
    }

    fun checkColorBlindnessConflict(colorA: Int, colorB: Int, type: ColorBlindnessType): Boolean {
        val simA = applyColorBlindness(colorA, type)
        val simB = applyColorBlindness(colorB, type)
        val dr = android.graphics.Color.red(simA) - android.graphics.Color.red(simB)
        val dg = android.graphics.Color.green(simA) - android.graphics.Color.green(simB)
        val db = android.graphics.Color.blue(simA) - android.graphics.Color.blue(simB)
        val distance = dr * dr + dg * dg + db * db
        return distance < 5000
    }

    fun generateShareBitmap(palette: List<PaletteColor>, title: String, width: Int = 1080, height: Int = 1920): Bitmap {
        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor("#F2F2F7".toColorInt())
        val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 72f
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        }
        val subtitlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = "#5F5F65".toColorInt()
            textSize = 36f
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        }
        val hexPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 32f
            typeface = android.graphics.Typeface.create("monospace", android.graphics.Typeface.NORMAL)
        }
        val brandPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = "#007AFF".toColorInt()
            textSize = 28f
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        }
        val margin = 80f
        var y = 200f
        val safeTitle = android.text.TextUtils.ellipsize(
            title,
            android.text.TextPaint(titlePaint),
            width - (margin * 2f),
            android.text.TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(safeTitle, margin, y, titlePaint)
        y += 60f
        canvas.drawText("${palette.size} colors · Campalette", margin, y, subtitlePaint)
        y += 120f
        val swatchHeight = (height - y.toInt() - 300) / palette.size.coerceAtLeast(1)
        palette.forEach { pc ->
            paint.color = pc.color
            canvas.drawRoundRect(margin, y, width - margin, y + swatchHeight - 20, 24f, 24f, paint)
            val textY = y + swatchHeight / 2f + 12f
            val textColor = if (android.graphics.Color.luminance(pc.color) > 0.5f) {
                android.graphics.Color.BLACK
            } else {
                android.graphics.Color.WHITE
            }
            hexPaint.color = textColor
            canvas.drawText("${pc.name}  ${pc.hexCode}", margin + 40f, textY, hexPaint)
            y += swatchHeight
        }
        canvas.drawText("CAMPALETTE", margin, height - 80f, brandPaint)
        return bitmap
    }

    // Palette extraction and the review screen only ever need ~1080px on the long
    // edge. Decoding the full-resolution JPEG and downscaling afterwards wastes
    // most of its time on pixels that get thrown away immediately, so we decode
    // straight to a sampled-down bitmap instead.
    private const val DECODE_TARGET_MAX_DIMENSION = 1080

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        if (image.format == ImageFormat.JPEG || image.planes.size == 1) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val decoded = decodeSampledBitmap(bytes, DECODE_TARGET_MAX_DIMENSION)
            return rotateBitmap(decoded, image.imageInfo.rotationDegrees)
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
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 92, out)
        val decoded = decodeSampledBitmap(out.toByteArray(), DECODE_TARGET_MAX_DIMENSION)

        return rotateBitmap(decoded, image.imageInfo.rotationDegrees)
    }

    /**
     * Decodes [bytes] directly to a bitmap no larger than roughly [maxDimension] on
     * its long edge, using [BitmapFactory.Options.inSampleSize] so the decoder skips
     * pixels during decode instead of allocating a full-resolution bitmap that then
     * gets thrown away by a separate downscale pass.
     */
    private fun decodeSampledBitmap(bytes: ByteArray, maxDimension: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw IllegalStateException("Bitmap decode failed")
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sampleSize = 1
        var sampledWidth = width
        var sampledHeight = height
        while (sampledWidth / 2 >= maxDimension && sampledHeight / 2 >= maxDimension) {
            sampledWidth /= 2
            sampledHeight /= 2
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }
}

enum class HarmonyMode(val label: String) {
    Analogous("Analogous Study"),
    Complementary("Complementary Study"),
    Tonal("Tonal Study")
}
