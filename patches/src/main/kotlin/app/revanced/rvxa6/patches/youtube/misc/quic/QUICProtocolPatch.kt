package app.revanced.rvxa6.patches.youtube.misc.quic

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.quic.baseQuicProtocolPatch
import app.revanced.rvxa6.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.rvxa6.patches.youtube.utils.patch.PatchList.DISABLE_QUIC_PROTOCOL
import app.revanced.rvxa6.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.rvxa6.patches.youtube.utils.settings.settingsPatch

@Suppress("unused", "SpellCheckingInspection")
val quicProtocolPatch = bytecodePatch(
    name = DISABLE_QUIC_PROTOCOL.patchName,
    description = DISABLE_QUIC_PROTOCOL.summary
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        settingsPatch,
        baseQuicProtocolPatch(),
    )

    apply {
        addPreference(
            arrayOf(
                "SETTINGS: DISABLE_QUIC_PROTOCOL"
            ),
            DISABLE_QUIC_PROTOCOL
        )
    }
}
