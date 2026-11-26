// Extension module: packaged as `extensions/rvxa6-spoof-wifi.rve`
// (folder name == extension file name; keep in sync with SpoofWifiPatch.kt).

android {
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly(libs.annotation)
}
