package app.revanced.rvxa6.patches.youtube.layout.dimming

import app.revanced.patcher.patch.resourcePatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.HIDE_SHORTS_DIMMING
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch
import app.revanced.rvxa6.util.removeOverlayBackground

@Suppress("unused")
val shortsDimmingPatch = resourcePatch(
    name = HIDE_SHORTS_DIMMING.patchName,
    description = HIDE_SHORTS_DIMMING.summary,
    use = false
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(settingsPatch)

    apply {

        removeOverlayBackground(
            arrayOf("reel_player_overlay_scrims.xml"),
            arrayOf("reel_player_overlay_v2_scrims_vertical")
        )
        removeOverlayBackground(
            arrayOf("reel_watch_fragment.xml"),
            arrayOf("reel_scrim_shorts_while_top")
        )

        addPreference(HIDE_SHORTS_DIMMING)

    }
}
