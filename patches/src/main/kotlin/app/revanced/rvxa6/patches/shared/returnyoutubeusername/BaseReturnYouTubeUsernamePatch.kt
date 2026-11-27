package app.revanced.rvxa6.patches.shared.returnyoutubeusername

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.extension.Constants.PATCHES_PATH
import app.revanced.rvxa6.patches.shared.textcomponent.hookSpannableString
import app.revanced.rvxa6.patches.shared.textcomponent.hookTextComponent
import app.revanced.rvxa6.patches.shared.textcomponent.textComponentPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "$PATCHES_PATH/ReturnYouTubeUsernamePatch;"

val baseReturnYouTubeUsernamePatch = bytecodePatch(
    description = "baseReturnYouTubeUsernamePatch"
) {
    dependsOn(textComponentPatch)

    apply {
        hookSpannableString(EXTENSION_CLASS_DESCRIPTOR, "preFetchLithoText")
        hookTextComponent(EXTENSION_CLASS_DESCRIPTOR)
    }
}

