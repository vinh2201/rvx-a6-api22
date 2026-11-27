package app.revanced.rvxa6.patches.youtube.utils.auth

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.extension.Constants.EXTENSION_PATH
import app.revanced.rvxa6.patches.youtube.utils.extension.sharedExtensionPatch
import app.revanced.rvxa6.patches.youtube.utils.request.buildRequestPatch
import app.revanced.rvxa6.patches.youtube.utils.request.hookBuildRequest
import app.revanced.rvxa6.util.fingerprint.methodOrThrow

private const val EXTENSION_AUTH_UTILS_CLASS_DESCRIPTOR =
    "$EXTENSION_PATH/innertube/utils/AuthUtils;"

val authHookPatch = bytecodePatch(
    description = "authHookPatch"
) {
    dependsOn(
        sharedExtensionPatch,
        buildRequestPatch,
    )

    apply {
        // Get incognito status and data sync id.
        accountIdentityFingerprint.methodOrThrow().addInstruction(
            1,
            "invoke-static {p3, p4}, $EXTENSION_AUTH_UTILS_CLASS_DESCRIPTOR->setDataSyncIdAndIncognitoStatus(Ljava/lang/String;Z)V"
        )

        // Get the header to use the auth token.
        hookBuildRequest("$EXTENSION_AUTH_UTILS_CLASS_DESCRIPTOR->setRequestHeaders(Ljava/lang/String;Ljava/util/Map;)V")
    }
}