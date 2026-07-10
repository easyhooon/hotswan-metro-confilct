plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dev.zacsweers.metro")
    id("com.github.skydoves.compose.hotswan.compiler")
}

android {
    namespace = "com.easyhooon.hotswanmetroconflict.feature"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.metrox.viewmodel)
    implementation(libs.metrox.viewmodel.compose)
}
