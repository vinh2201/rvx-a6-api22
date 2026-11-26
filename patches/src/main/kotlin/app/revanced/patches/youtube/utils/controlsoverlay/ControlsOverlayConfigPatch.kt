package app.revanced.patches.youtube.utils.controlsoverlay

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.fingerprint.methodOrThrow
import app.revanced.util.fingerprint.resolvable
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

val controlsOverlayConfigPatch = bytecodePatch(
    description = "controlsOverlayConfigPatch"
) {

    apply {
        /**
         * Added in YouTube v18.39.41
         *
         * No exception even if fail to resolve fingerprints.
         * For compatibility with YouTube v18.25.40 ~ YouTube v18.38.44.
         */
        if (controlsOverlayConfigFingerprint.resolvable()) {
            controlsOverlayConfigFingerprint.methodOrThrow().apply {
                val targetIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex,
                    "const/4 v$targetRegister, 0x0"
                )
            }
        }
    }
}
