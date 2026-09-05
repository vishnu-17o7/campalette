import java.io.OutputStream
import java.security.CodeSigner
import java.util.Properties
import java.util.jar.JarFile

val releaseKeystorePropertiesFile = rootProject.file("keystore.properties")
val releaseKeystoreProperties = Properties().apply {
    if (releaseKeystorePropertiesFile.isFile) {
        releaseKeystorePropertiesFile.inputStream().use(::load)
    }
}
val releaseSigningKeys = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
val missingReleaseSigningKeys = releaseSigningKeys.filter {
    releaseKeystoreProperties.getProperty(it).isNullOrBlank()
}
val releaseStoreFile = releaseKeystoreProperties.getProperty("storeFile")
    ?.takeIf(String::isNotBlank)
    ?.let(rootProject::file)
val hasReleaseSigning = releaseKeystorePropertiesFile.isFile &&
    missingReleaseSigningKeys.isEmpty() &&
    releaseStoreFile?.isFile == true

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.vishnu.campalette"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vishnu.campalette"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("boolean", "BENCHMARK_MODE", "false")
        manifestPlaceholders["profileableByShell"] = "false"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseKeystoreProperties.getProperty("storePassword")
                keyAlias = releaseKeystoreProperties.getProperty("keyAlias")
                keyPassword = releaseKeystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
            manifestPlaceholders["profileableByShell"] = "false"
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            buildConfigField("boolean", "BENCHMARK_MODE", "true")
            manifestPlaceholders["profileableByShell"] = "true"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    
    // Material 3
    implementation("com.google.android.material:material:1.14.0")
    
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.13.0")

    // Live backdrop blur for the floating glass dock and camera overlays.
    implementation("dev.chrisbanes.haze:haze:1.7.2")
    implementation("dev.chrisbanes.haze:haze-materials:1.7.2")
    
    // CameraX
    implementation("androidx.camera:camera-camera2:1.6.1")
    implementation("androidx.camera:camera-lifecycle:1.6.1")
    implementation("androidx.camera:camera-view:1.6.1")
    
    // Palette API for color extraction
    implementation("androidx.palette:palette-ktx:1.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    baselineProfile(project(":benchmark"))
}

val verifyReleaseSigning by tasks.registering {
    group = "verification"
    description = "Checks that the owner upload keystore is configured for a publishable bundle."

    doLast {
        check(releaseKeystorePropertiesFile.isFile) {
            "Missing keystore.properties. Copy keystore.properties.example and add the owner upload-key values."
        }
        check(missingReleaseSigningKeys.isEmpty()) {
            "keystore.properties is missing: ${missingReleaseSigningKeys.joinToString()}."
        }
        check(releaseStoreFile?.isFile == true) {
            "Upload keystore does not exist at ${releaseStoreFile?.absolutePath ?: "<missing storeFile>"}."
        }
    }
}

tasks.matching { it.name == "bundleRelease" }.configureEach {
    mustRunAfter(verifyReleaseSigning)
}

tasks.register("signedBundleRelease") {
    group = "build"
    description = "Builds the release App Bundle and verifies its upload-key signature."
    dependsOn(verifyReleaseSigning, "bundleRelease")

    doLast {
        val bundle = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
        check(bundle.isFile) { "Release bundle was not produced at ${bundle.absolutePath}." }

        val signatureAudit = JarFile(bundle, true).use { jar ->
            val entries = jar.entries()
            var payloadEntryCount = 0
            var expectedSigners: Set<CodeSigner>? = null
            val unsignedEntries = mutableListOf<String>()
            val inconsistentSignerEntries = mutableListOf<String>()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.isDirectory && !entry.name.startsWith("META-INF/")) {
                    payloadEntryCount += 1
                    // Reading the complete entry makes JarFile verify its digest before exposing
                    // the signer chain. A tampered entry therefore fails this task immediately.
                    jar.getInputStream(entry).use { input ->
                        input.copyTo(OutputStream.nullOutputStream())
                    }

                    val entrySigners = entry.codeSigners?.toSet().orEmpty()
                    when {
                        entrySigners.isEmpty() -> unsignedEntries += entry.name
                        expectedSigners == null -> expectedSigners = entrySigners
                        entrySigners != expectedSigners -> inconsistentSignerEntries += entry.name
                    }
                }
            }

            Triple(payloadEntryCount, unsignedEntries, inconsistentSignerEntries)
        }

        val (payloadEntryCount, unsignedEntries, inconsistentSignerEntries) = signatureAudit
        check(payloadEntryCount > 0) {
            "The release App Bundle contains no payload entries. Refusing to treat it as publishable."
        }
        check(unsignedEntries.isEmpty()) {
            "The release App Bundle contains unsigned payload entries: " +
                unsignedEntries.take(5).joinToString() +
                if (unsignedEntries.size > 5) " (+${unsignedEntries.size - 5} more)." else "."
        }
        check(inconsistentSignerEntries.isEmpty()) {
            "The release App Bundle contains payload entries signed by a different signer: " +
                inconsistentSignerEntries.take(5).joinToString() +
                if (inconsistentSignerEntries.size > 5) {
                    " (+${inconsistentSignerEntries.size - 5} more)."
                } else {
                    "."
                }
        }
        logger.lifecycle(
            "Verified every payload entry in signed release bundle: ${bundle.absolutePath}"
        )
    }
}
