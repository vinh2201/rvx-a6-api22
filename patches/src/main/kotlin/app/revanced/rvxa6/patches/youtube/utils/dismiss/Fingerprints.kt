package app.revanced.rvxa6.patches.youtube.utils.dismiss

import app.revanced.rvxa6.util.fingerprint.legacyFingerprint
import app.revanced.rvxa6.util.or
import com.android.tools.smali.dexlib2.AccessFlags

internal const val DISMISS_PLAYER_LITERAL = 34699L

internal val dismissPlayerOnClickListenerFingerprint = legacyFingerprint(
    name = "dismissPlayerOnClickListenerFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    literals = listOf(DISMISS_PLAYER_LITERAL),
    customFingerprint = { method, _ ->
        method.name == "onClick"
    }
)
