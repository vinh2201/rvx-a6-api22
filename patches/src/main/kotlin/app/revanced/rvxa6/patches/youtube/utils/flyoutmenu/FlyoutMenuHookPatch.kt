package app.revanced.rvxa6.patches.youtube.utils.flyoutmenu

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.extension.Constants.EXTENSION_PATH
import app.revanced.rvxa6.patches.youtube.utils.playbackRateBottomSheetBuilderFingerprint
import app.revanced.rvxa6.patches.youtube.utils.resourceid.sharedResourceIdPatch
import app.revanced.rvxa6.util.addStaticFieldToExtension
import app.revanced.rvxa6.util.fingerprint.methodOrThrow

private const val EXTENSION_VIDEO_UTILS_CLASS_DESCRIPTOR =
    "$EXTENSION_PATH/utils/VideoUtils;"

val flyoutMenuHookPatch = bytecodePatch(
    description = "flyoutMenuHookPatch"
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(sharedResourceIdPatch)

    apply {
        playbackRateBottomSheetBuilderFingerprint.methodOrThrow().apply {
            val smaliInstructions =
                """
                    if-eqz v0, :ignore
                    invoke-virtual {v0}, $definingClass->$name()V
                    :ignore
                    return-void
                    """

            addStaticFieldToExtension(
                EXTENSION_VIDEO_UTILS_CLASS_DESCRIPTOR,
                "showYouTubeLegacyPlaybackSpeedFlyoutMenu",
                "playbackRateBottomSheetClass",
                definingClass,
                smaliInstructions
            )
        }

        videoQualityBottomSheetClassFingerprint.methodOrThrow().apply {
            val smaliInstructions =
                """
                    if-eqz v0, :ignore
                    invoke-virtual {v0}, $definingClass->$name()V
                    :ignore
                    return-void
                    """

            addStaticFieldToExtension(
                EXTENSION_VIDEO_UTILS_CLASS_DESCRIPTOR,
                "showYouTubeLegacyVideoQualityFlyoutMenu",
                "videoQualityBottomSheetClass",
                definingClass,
                smaliInstructions
            )
        }
    }
}
