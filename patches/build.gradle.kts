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

// ---- Core library desugaring (java.time, streams, Map/Collection defaults on Android 6-7) ----
// The extension is compiled with core library desugaring, so it references `j$.*` classes. Those classes are
// produced by AGP's L8 step as a separate dex which must be shipped as `extensions/rvxa6-desugarlib.rve`.
//
// AGP >= 8 puts that dex under `desugar_lib_dex/<variant>/<task>/classes1000.dex` (the old a6 path
// `desugar_lib_dex/release/classes1000.dex` no longer exists), so it is searched for instead of hard-coded.
//
// Manual override: drop a ready-made `rvxa6-desugarlib.rve` (e.g. the `desugarlib.rve` taken from an original
// a6 release) into `patches/src/main/resources/extensions/` and it is used as is.
val sharedExtension = project(":extensions:rvxa6-shared")
val manualDesugarLib = layout.projectDirectory.file("src/main/resources/extensions/rvxa6-desugarlib.rve").asFile

fun findDesugarLibDex(): File? = fileTree(sharedExtension.layout.buildDirectory) {
    include("**/desugar_lib_dex/**/*.dex", "**/classes1000.dex")
}.files.sortedWith(compareBy({ !it.path.contains("release", ignoreCase = true) }, { it.path })).firstOrNull()

tasks {
    jar {
        // Build the extension (and the L8 desugar dex, if the variant has such a task) before packaging.
        dependsOn(provider {
            sharedExtension.tasks.matching { it.name.startsWith("l8DexDesugarLib") }
        })

        from(provider { listOfNotNull(if (manualDesugarLib.exists()) null else findDesugarLibDex()) }) {
            into("extensions")
            rename { "rvxa6-desugarlib.rve" }
        }

        doFirst {
            if (!manualDesugarLib.exists() && findDesugarLibDex() == null) {
                val dexFiles = fileTree(sharedExtension.layout.buildDirectory) { include("**/*.dex") }.files
                    .joinToString("\n") { "  - ${it.relativeTo(rootDir)}" }
                    .ifEmpty { "  (none)" }
                throw GradleException(
                    "rvxa6-desugarlib.rve could not be produced: no L8 desugar dex (desugar_lib_dex/**/*.dex) was found " +
                        "in extensions/rvxa6-shared/build.\n" +
                        "Dex files that do exist there:\n$dexFiles\n" +
                        "Either send this list, or copy an original a6 `desugarlib.rve` to " +
                        "patches/src/main/resources/extensions/rvxa6-desugarlib.rve."
                )
            }
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
