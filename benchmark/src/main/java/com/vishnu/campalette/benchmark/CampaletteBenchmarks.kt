package com.vishnu.campalette.benchmark

import android.content.Intent
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE_NAME = "com.vishnu.campalette"
private const val MAIN_ACTIVITY = "$PACKAGE_NAME.MainActivity"
private const val SEED_LIBRARY_EXTRA = "campalette.benchmark.SEED_LIBRARY"
private const val UI_TIMEOUT = 8_000L

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun criticalUserJourneys() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        includeInStartupProfile = true
    ) {
        grantCameraPermission()
        pressHome()
        startActivityAndWait(seededLaunchIntent())

        // Exercise live camera composition and the touch-to-sample path.
        device.wait(Until.hasObject(By.descContains("Camera preview")), UI_TIMEOUT)
        device.click(device.displayWidth / 2, device.displayHeight * 42 / 100)
        device.waitForIdle()

        // Exercise the largest lazy collection and its alternate layout.
        device.findObject(By.desc("Library"))?.click()
        device.wait(Until.hasObject(By.text("Library")), UI_TIMEOUT)
        scrollLibrary(device)
        device.findObject(By.desc("Toggle library view"))?.click()
        device.waitForIdle()

        // Include the settings surface and return-to-camera navigation path.
        device.findObject(By.desc("Settings"))?.click()
        device.wait(Until.hasObject(By.text("Settings")), UI_TIMEOUT)
        device.findObject(By.desc("Camera"))?.click()
        device.waitForIdle()
    }
}

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupNoCompilation() = measureStartup(CompilationMode.None())

    @Test
    fun coldStartupWithBaselineProfile() = measureStartup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    private fun measureStartup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = 8,
        setupBlock = {
            grantCameraPermission()
            pressHome()
        }
    ) {
        startActivityAndWait()
    }
}

@RunWith(AndroidJUnit4::class)
class LibraryScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollLibrary() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = BaselineProfileMode.Require
        ),
        iterations = 6,
        setupBlock = {
            grantCameraPermission()
            pressHome()
            startActivityAndWait(seededLaunchIntent())
            device.findObject(By.desc("Library"))?.click()
            device.wait(Until.hasObject(By.text("Library")), UI_TIMEOUT)
        }
    ) {
        scrollLibrary(device)
    }
}

private fun seededLaunchIntent(): Intent = Intent(Intent.ACTION_MAIN).apply {
    setClassName(PACKAGE_NAME, MAIN_ACTIVITY)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    putExtra(SEED_LIBRARY_EXTRA, true)
}

private fun grantCameraPermission() {
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        .executeShellCommand("pm grant $PACKAGE_NAME android.permission.CAMERA")
}

private fun scrollLibrary(device: UiDevice) {
    val x = device.displayWidth / 2
    val top = device.displayHeight * 35 / 100
    val bottom = device.displayHeight * 82 / 100
    repeat(5) {
        device.swipe(x, bottom, x, top, 18)
    }
    repeat(3) {
        device.swipe(x, top, x, bottom, 18)
    }
    device.waitForIdle()
}
