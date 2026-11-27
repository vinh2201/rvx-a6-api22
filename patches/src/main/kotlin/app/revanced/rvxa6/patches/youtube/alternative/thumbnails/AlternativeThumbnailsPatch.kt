package app.revanced.rvxa6.patches.youtube.alternative.thumbnails

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.imageurl.addImageUrlErrorCallbackHook
import app.revanced.rvxa6.patches.shared.imageurl.addImageUrlHook
import app.revanced.rvxa6.patches.shared.imageurl.addImageUrlSuccessCallbackHook
import app.revanced.rvxa6.patches.shared.imageurl.cronetImageUrlHookPatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.extension.Constants.ALTERNATIVE_THUMBNAILS_CLASS_DESCRIPTOR
import app.revanced.rvxa6.patches.youtube.utils.navigation.navigationBarHookPatch
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.ALTERNATIVE_THUMBNAILS
import app.revanced.rvxa6.patches.youtube.utils.playertype.playerTypeHookPatch
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch

@Suppress("unused")
val alternativeThumbnailsPatch = bytecodePatch(
    name = ALTERNATIVE_THUMBNAILS.patchName,
    description = ALTERNATIVE_THUMBNAILS.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        cronetImageUrlHookPatch(true),
        navigationBarHookPatch,
        playerTypeHookPatch,
        settingsPatch,
    )
    apply {

        addImageUrlHook(ALTERNATIVE_THUMBNAILS_CLASS_DESCRIPTOR)
        addImageUrlSuccessCallbackHook(ALTERNATIVE_THUMBNAILS_CLASS_DESCRIPTOR)
        addImageUrlErrorCallbackHook(ALTERNATIVE_THUMBNAILS_CLASS_DESCRIPTOR)

        // region add settings

        addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: ALTERNATIVE_THUMBNAILS",
                "SETTINGS: ALTERNATIVE_THUMBNAILS"
            ),
            ALTERNATIVE_THUMBNAILS
        )

        // endregion

    }
}
