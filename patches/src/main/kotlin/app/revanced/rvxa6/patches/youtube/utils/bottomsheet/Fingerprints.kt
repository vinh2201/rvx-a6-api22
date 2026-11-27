package app.revanced.rvxa6.patches.youtube.utils.bottomsheet

import app.revanced.rvxa6.patches.youtube.utils.resourceid.designBottomSheet
import app.revanced.rvxa6.util.fingerprint.legacyFingerprint

internal val bottomSheetBehaviorFingerprint = legacyFingerprint(
    name = "bottomSheetBehaviorFingerprint",
    returnType = "V",
    literals = listOf(designBottomSheet),
)
