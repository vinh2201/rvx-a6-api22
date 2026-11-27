package app.revanced.rvxa6.patches.youtube.misc.codecs

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.opus.baseOpusCodecsPatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.ENABLE_OPUS_CODEC
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch

@Suppress("unused")
val opusCodecPatch = bytecodePatch(
    name = ENABLE_OPUS_CODEC.patchName,
    description = ENABLE_OPUS_CODEC.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        settingsPatch,
        baseOpusCodecsPatch(),
    )

    apply {
        addPreference(
            arrayOf(
                "PREFERENCE_CATEGORY: MISC_EXPERIMENTAL_FLAGS",
                "SETTINGS: ENABLE_OPUS_CODEC"
            ),
            ENABLE_OPUS_CODEC
        )
    }
}
