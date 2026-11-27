package app.revanced.rvxa6.patches.youtube.general.audiotracks

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.youtube.utils.audiotracks.audioTracksHookPatch
import app.revanced.rvxa6.patches.youtube.utils.audiotracks.disableForcedAudioTracks
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.DISABLE_FORCED_AUTO_AUDIO_TRACKS
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch

@Suppress("unused")
val audioTracksPatch = bytecodePatch(
    name = DISABLE_FORCED_AUTO_AUDIO_TRACKS.patchName,
    description = DISABLE_FORCED_AUTO_AUDIO_TRACKS.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        settingsPatch,
        audioTracksHookPatch,
    )

    apply {

        disableForcedAudioTracks()

        // region add settings

        addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: GENERAL",
                "SETTINGS: DISABLE_AUTO_AUDIO_TRACKS"
            ),
            DISABLE_FORCED_AUTO_AUDIO_TRACKS
        )

        // endregion

    }
}
