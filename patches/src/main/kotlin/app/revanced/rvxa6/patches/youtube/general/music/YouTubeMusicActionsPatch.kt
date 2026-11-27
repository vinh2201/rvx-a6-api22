package app.revanced.rvxa6.patches.youtube.general.music

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.extension.Constants.GENERAL_PATH
import app.revanced.rvxa6.patches.youtube.utils.extension.Constants.PATCH_STATUS_CLASS_DESCRIPTOR
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.GMSCORE_SUPPORT
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.HOOK_YOUTUBE_MUSIC_ACTIONS
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.getContext
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.youtubeMusicPackageName
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch
import app.revanced.rvxa6.util.addEntryValues
import app.revanced.rvxa6.util.findMethodOrThrow
import app.revanced.rvxa6.util.fingerprint.matchOrThrow
import app.revanced.rvxa6.util.getReference
import app.revanced.rvxa6.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "$GENERAL_PATH/YouTubeMusicActionsPatch;"

@Suppress("unused")
val youtubeMusicActionsPatch = bytecodePatch(
    name = HOOK_YOUTUBE_MUSIC_ACTIONS.patchName,
    description = HOOK_YOUTUBE_MUSIC_ACTIONS.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(settingsPatch)

    apply {

        appDeepLinkFingerprint.matchOrThrow().let {
            it.method.apply {
                val packageNameIndex = it.patternMatch!!.startIndex
                val packageNameField =
                    getInstruction<ReferenceInstruction>(packageNameIndex).reference.toString()

                implementation!!.instructions
                    .withIndex()
                    .filter { (_, instruction) ->
                        instruction.opcode == Opcode.IGET_OBJECT &&
                                instruction.getReference<FieldReference>()
                                    ?.toString() == packageNameField
                    }
                    .map { (index, _) -> index }
                    .reversed()
                    .forEach { index ->
                        val register = getInstruction<TwoRegisterInstruction>(index).registerA

                        addInstructions(
                            index + 1, """
                                invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->overridePackageName(Ljava/lang/String;)Ljava/lang/String;
                                move-result-object v$register
                                """
                        )
                    }
            }
        }

        // region add settings

        addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: GENERAL",
                "SETTINGS: HOOK_BUTTONS",
                "SETTINGS: HOOK_YOUTUBE_MUSIC_ACTIONS"
            ),
            HOOK_YOUTUBE_MUSIC_ACTIONS
        )

        // endregion

    }

    afterDependents {
        if (GMSCORE_SUPPORT.included == true) {
            getContext().apply {
                addEntryValues(
                    "revanced_third_party_youtube_music_label",
                    "RVX Music"
                )
                addEntryValues(
                    "revanced_third_party_youtube_music_package_name",
                    youtubeMusicPackageName
                )
            }
            findMethodOrThrow(PATCH_STATUS_CLASS_DESCRIPTOR) {
                name == "RVXMusicPackageName"
            }.apply {
                val replaceIndex = indexOfFirstInstructionOrThrow(Opcode.CONST_STRING)
                val replaceRegister =
                    getInstruction<OneRegisterInstruction>(replaceIndex).registerA

                replaceInstruction(
                    replaceIndex,
                    "const-string v$replaceRegister, \"$youtubeMusicPackageName\""
                )
            }
        }
    }
}
