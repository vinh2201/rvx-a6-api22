package app.revanced.rvxa6.patches.youtube.alternative.thumbnails

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.imageurl.addImageUrlHook
import app.revanced.rvxa6.patches.shared.imageurl.cronetImageUrlHookPatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.BYPASS_IMAGE_REGION_RESTRICTIONS
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch

@Suppress("unused")
val bypassImageRegionRestrictionsPatch = bytecodePatch(
    name = BYPASS_IMAGE_REGION_RESTRICTIONS.patchName,
    description = BYPASS_IMAGE_REGION_RESTRICTIONS.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        cronetImageUrlHookPatch(true),
        settingsPatch,
    )
    apply {

        addImageUrlHook()

        // region add settings

        addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: ALTERNATIVE_THUMBNAILS",
                "SETTINGS: BYPASS_IMAGE_REGION_RESTRICTIONS"
            ),
            BYPASS_IMAGE_REGION_RESTRICTIONS
        )

        // endregion

    }
}
