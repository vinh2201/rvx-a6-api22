package app.revanced.rvxa6.patches.youtube.misc.watchhistory

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.trackingurlhook.hookWatchHistory
import app.revanced.rvxa6.patches.shared.trackingurlhook.trackingUrlHookPatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.WATCH_HISTORY
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch

@Suppress("unused")
val watchHistoryPatch = bytecodePatch(
    name = WATCH_HISTORY.patchName,
    description = WATCH_HISTORY.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        settingsPatch,
        trackingUrlHookPatch,
    )

    apply {

        hookWatchHistory()

        // region add settings

        addPreference(
            arrayOf(
                "SETTINGS: WATCH_HISTORY"
            ),
            WATCH_HISTORY
        )

        // endregion

    }
}
