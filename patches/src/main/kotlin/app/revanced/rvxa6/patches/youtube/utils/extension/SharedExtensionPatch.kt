package app.revanced.rvxa6.patches.youtube.utils.extension

import app.revanced.rvxa6.patches.shared.extension.sharedExtensionPatch
import app.revanced.rvxa6.patches.youtube.utils.extension.hooks.applicationInitHook

// TODO: Move this to a "Hook.kt" file. Same for other extension hook patches.
val sharedExtensionPatch = sharedExtensionPatch(
    applicationInitHook,
)
