package app.revanced.patches.youtube.shorts.seek

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.patch.PatchList.SHORTS_SEEK
import app.revanced.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.patches.youtube.utils.toolbar.hookToolBar
import app.revanced.patches.youtube.utils.toolbar.toolBarHookPatch

@Suppress("unused")
val shortsSeekPatch = bytecodePatch(
    name = SHORTS_SEEK.patchName,
    description = SHORTS_SEEK.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(toolBarHookPatch)

    apply {
        hookToolBar("Lapp/revanced/extension/youtube/patches/shorts/ShortsSeekPatch;->replaceToolbarButton")

        addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: SHORTS",
                "SETTINGS: SHORTS_SEEK",
            ),
            SHORTS_SEEK
        )
    }
}