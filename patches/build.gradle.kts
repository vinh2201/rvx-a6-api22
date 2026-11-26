group = "app.revanced"

// In GitHub Actions GITHUB_REPOSITORY is "<owner>/<repo>", so the links below fill themselves in.
val repositorySlug = System.getenv("GITHUB_REPOSITORY") ?: "OWNER/revanced-patches-android6"

patches {
    about {
        name = "YouTube ReVanced for Android 6-7 (A6)"
        description = "RVX Patches for YouTube 17.34.36 (Android 6-7), ported to ReVanced patcher v22"
        source = "https://github.com/$repositorySlug"
        author = "kitadai31 / inotia00 / ReVanced (ported to patcher v22)"
        contact = "https://github.com/$repositorySlug/issues"
        website = "https://github.com/$repositorySlug"
        license = "GNU General Public License v3.0"
    }
}

dependencies {
    // Required due to smali, or build fails. Can be removed once smali is bumped.
    implementation(libs.guava)

    // Android API stubs defined here.
    compileOnly(project(":patches:stub"))
}

tasks {
    jar {
        // A classes.dex from CoreLibraryDesugaring (built by the rvxa6-shared extension).
        from("../extensions/rvxa6-shared/build/intermediates/desugar_lib_dex/release/classes1000.dex") {
            into("extensions")
            rename { "rvxa6-desugarlib.rve" }
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexplicit-backing-fields",
            "-Xcontext-parameters"
        )
    }
}

publishing {
    repositories {
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/$repositorySlug")
            credentials(PasswordCredentials::class)
        }
    }
}
