package app.revanced.rvxa6.patches.youtube.utils.auth

import app.revanced.rvxa6.util.fingerprint.legacyFingerprint
import app.revanced.rvxa6.util.or
import com.android.tools.smali.dexlib2.AccessFlags

internal val accountIdentityFingerprint = legacyFingerprint(
    name = "accountIdentityFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    customFingerprint = { method, _ ->
        method.definingClass.endsWith("${'$'}AutoValue_AccountIdentity;")
    }
)
