package app.revanced.rvxa6.patches.youtube.layout.theme

import app.revanced.patcher.patch.resourcePatch
import app.revanced.rvxa6.patches.shared.materialyou.baseMaterialYou
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.MATERIALYOU
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.updatePatchStatusTheme
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch
import app.revanced.rvxa6.util.copyXmlNode

@Suppress("unused")
val materialYouPatch = resourcePatch(
    name = MATERIALYOU.patchName,
    description = MATERIALYOU.summary,
    use = false
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        sharedThemePatch,
        settingsPatch,
    )

    apply {
        baseMaterialYou()

        copyXmlNode("youtube/materialyou/host", "values-v31/colors.xml", "resources")

        updatePatchStatusTheme("MaterialYou")

        addPreference(MATERIALYOU)

    }
}
