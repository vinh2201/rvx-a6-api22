package app.revanced.rvxa6.patches.shared.spoof.useragent

import app.revanced.rvxa6.util.fingerprint.legacyFingerprint
import app.revanced.rvxa6.util.or
import com.android.tools.smali.dexlib2.AccessFlags

const val CLIENT_PACKAGE_NAME = "cbr"

internal val apiStatsFingerprint = legacyFingerprint(
    name = "apiStatsFingerprint",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    returnType = "V",
    strings = listOf(CLIENT_PACKAGE_NAME),
)
