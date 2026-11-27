package app.revanced.rvxa6.patches.youtube.utils.flyoutmenu

import app.revanced.rvxa6.patches.youtube.utils.resourceid.videoQualityUnavailableAnnouncement
import app.revanced.rvxa6.util.fingerprint.legacyFingerprint
import app.revanced.rvxa6.util.or
import com.android.tools.smali.dexlib2.AccessFlags

internal val videoQualityBottomSheetClassFingerprint = legacyFingerprint(
    name = "videoQualityBottomSheetClassFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = emptyList(),
    literals = listOf(videoQualityUnavailableAnnouncement),
)
