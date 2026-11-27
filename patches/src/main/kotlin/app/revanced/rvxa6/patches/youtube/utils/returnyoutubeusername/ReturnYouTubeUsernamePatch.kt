package app.revanced.rvxa6.patches.youtube.utils.returnyoutubeusername

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.returnyoutubeusername.baseReturnYouTubeUsernamePatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.RETURN_YOUTUBE_USERNAME
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch

@Suppress("unused")
val returnYouTubeUsernamePatch = bytecodePatch(
    name = RETURN_YOUTUBE_USERNAME.patchName,
    description = RETURN_YOUTUBE_USERNAME.summary,
    use = false
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        baseReturnYouTubeUsernamePatch,
        settingsPatch,
    )

    apply {

        // region add settings

        addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: RETURN_YOUTUBE_USERNAME"
            ),
            RETURN_YOUTUBE_USERNAME
        )

        // endregion
    }
}
