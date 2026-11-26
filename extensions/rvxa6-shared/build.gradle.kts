// Extension module: packaged by the ReVanced patches plugin as `extensions/rvxa6-shared.rve`.
// (Folder name == extension file name; keep in sync with SharedExtensionPatch.kt.)

android {
    defaultConfig {
        // YouTube 17.34.36 supports Android 6.0+.
        minSdk = 23
        multiDexEnabled = true
    }

    compileOptions {
        // Needed for java.time/streams on Android 6-7, the dex is shipped as `rvxa6-desugarlib.rve`.
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly(libs.annotation)
    compileOnly(libs.preference)
    implementation(libs.collections4)
    implementation(libs.lang3)
    implementation(libs.okhttp3)

    coreLibraryDesugaring(libs.desugar)
    compileOnly(project(":extensions:rvxa6-shared:stub"))
}
