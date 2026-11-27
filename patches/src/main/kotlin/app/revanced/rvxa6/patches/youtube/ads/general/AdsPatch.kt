package app.revanced.rvxa6.patches.youtube.ads.general

import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.rvxa6.patches.shared.ads.baseAdsPatch
import app.revanced.rvxa6.patches.shared.ads.hookLithoFullscreenAds
import app.revanced.rvxa6.patches.shared.ads.hookNonLithoFullscreenAds
import app.revanced.rvxa6.patches.shared.litho.addLithoFilter
import app.revanced.rvxa6.patches.shared.litho.lithoFilterPatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.extension.Constants.ADS_CLASS_DESCRIPTOR
import app.revanced.rvxa6.patches.youtube.utils.extension.Constants.COMPONENTS_PATH
import app.revanced.rvxa6.patches.youtube.utils.fix.litho.lithoLayoutPatch
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.HIDE_ADS
import app.revanced.rvxa6.patches.youtube.utils.resourceid.adAttribution
import app.revanced.rvxa6.patches.youtube.utils.resourceid.interstitialsContainer
import app.revanced.rvxa6.patches.youtube.utils.resourceid.sharedResourceIdPatch
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch
import app.revanced.rvxa6.util.findMutableMethodOf
import app.revanced.rvxa6.util.fingerprint.matchOrThrow
import app.revanced.rvxa6.util.fingerprint.methodOrThrow
import app.revanced.rvxa6.util.injectHideViewCall
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c

private const val ADS_FILTER_CLASS_DESCRIPTOR =
    "$COMPONENTS_PATH/AdsFilter;"

@Suppress("unused")
val adsPatch = bytecodePatch(
    name = HIDE_ADS.patchName,
    description = HIDE_ADS.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        settingsPatch,
        baseAdsPatch(ADS_CLASS_DESCRIPTOR, "hideVideoAds"),
        lithoFilterPatch,
        lithoLayoutPatch,
        sharedResourceIdPatch,
    )

    apply {
        addLithoFilter(ADS_FILTER_CLASS_DESCRIPTOR)

        // region patch for hide fullscreen ads

        // non-litho view, used in some old clients
        interstitialsContainerFingerprint
            .methodOrThrow()
            .hookNonLithoFullscreenAds(interstitialsContainer)

        // litho view, used in 'ShowDialogCommandOuterClass' in innertube
        showDialogCommandFingerprint
            .matchOrThrow()
            .hookLithoFullscreenAds()

        // endregion

        // region patch for hide general ads

        classDefs.toList().forEach { classDef ->
            classDef.methods.forEach { method ->
                method.implementation.apply {
                    this?.instructions?.forEachIndexed { index, instruction ->
                        if (instruction.opcode != Opcode.CONST)
                            return@forEachIndexed
                        // Instruction to store the id adAttribution into a register
                        if ((instruction as Instruction31i).wideLiteral != adAttribution)
                            return@forEachIndexed

                        val insertIndex = index + 1

                        // Call to get the view with the id adAttribution
                        (instructions.elementAt(insertIndex)).apply {
                            if (opcode != Opcode.INVOKE_VIRTUAL)
                                return@forEachIndexed

                            // Hide the view
                            val viewRegister = (this as Instruction35c).registerC
                            classDefs.getOrReplaceMutable(classDef)
                                .findMutableMethodOf(method)
                                .injectHideViewCall(
                                    insertIndex,
                                    viewRegister,
                                    ADS_CLASS_DESCRIPTOR,
                                    "hideAdAttributionView"
                                )
                        }
                    }
                }
            }
        }

        // endregion

        // region patch for hide get premium

        compactYpcOfferModuleViewFingerprint.matchOrThrow().let {
            it.method.apply {
                val startIndex = it.patternMatch!!.startIndex
                val measuredWidthRegister =
                    getInstruction<TwoRegisterInstruction>(startIndex).registerA
                val measuredHeightInstruction =
                    getInstruction<TwoRegisterInstruction>(startIndex + 1)
                val measuredHeightRegister = measuredHeightInstruction.registerA
                val tempRegister = measuredHeightInstruction.registerB

                addInstructionsWithLabels(
                    startIndex + 2, """
                        invoke-static {}, $ADS_CLASS_DESCRIPTOR->hideGetPremium()Z
                        move-result v$tempRegister
                        if-eqz v$tempRegister, :show
                        const/4 v$measuredWidthRegister, 0x0
                        const/4 v$measuredHeightRegister, 0x0
                        """, ExternalLabel("show", getInstruction(startIndex + 2))
                )
            }
        }

        // endregion

        // region patch for hide end screen store banner

        fullScreenEngagementAdContainerFingerprint.methodOrThrow().apply {
            val addListIndex = indexOfAddListInstruction(this)
            val addListInstruction =
                getInstruction<FiveRegisterInstruction>(addListIndex)
            val listRegister = addListInstruction.registerC
            val objectRegister = addListInstruction.registerD

            replaceInstruction(
                addListIndex,
                "invoke-static { v$listRegister, v$objectRegister }, " +
                        "$ADS_CLASS_DESCRIPTOR->hideEndScreenStoreBanner(Ljava/util/List;Ljava/lang/Object;)V"
            )
        }

        // endregion

        // region add settings

        addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: ADS"
            ),
            HIDE_ADS
        )

        // endregion

    }
}
