package app.revanced.rvxa6.patches.shared.viewgroup

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.extension.Constants.EXTENSION_UTILS_CLASS_DESCRIPTOR
import app.revanced.rvxa6.util.findMethodOrThrow
import app.revanced.rvxa6.util.fingerprint.methodOrThrow

val viewGroupMarginLayoutParamsHookPatch = bytecodePatch(
    description = "viewGroupMarginLayoutParamsHookPatch"
) {
    apply {
        val setViewGroupMarginCall = with(
            viewGroupMarginFingerprint.methodOrThrow(viewGroupMarginParentFingerprint)
        ) {
            "$definingClass->$name(Landroid/view/View;II)V"
        }

        findMethodOrThrow(EXTENSION_UTILS_CLASS_DESCRIPTOR) {
            name == "hideViewGroupByMarginLayoutParams"
        }.addInstructions(
            0, """
                const/4 v0, 0x0
                invoke-static {p0, v0, v0}, $setViewGroupMarginCall
                """
        )
    }
}

