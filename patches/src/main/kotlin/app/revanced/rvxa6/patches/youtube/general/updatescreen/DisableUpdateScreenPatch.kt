package app.revanced.rvxa6.patches.youtube.general.updatescreen

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.youtube.general.components.appBlockingCheckResultToStringFingerprint
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.DISABLE_UPDATE_SCREEN
import app.revanced.rvxa6.util.fingerprint.mutableClassOrThrow
import com.android.tools.smali.dexlib2.util.MethodUtil

val disableUpdateScreen = bytecodePatch(
    name = DISABLE_UPDATE_SCREEN.patchName,
    description = DISABLE_UPDATE_SCREEN.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    apply {
        appBlockingCheckResultToStringFingerprint.mutableClassOrThrow().methods.first { method ->
            MethodUtil.isConstructor(method) &&
                    method.parameters == listOf("Landroid/content/Intent;", "Z")
        }.addInstructions(
            1,
            "const/4 p1, 0x0"
        )
    }
}