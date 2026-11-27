package app.revanced.rvxa6.patches.shared.spans

import app.revanced.rvxa6.util.fingerprint.legacyFingerprint

internal val customCharacterStyleFingerprint = legacyFingerprint(
    name = "customCharacterStyleFingerprint",
    returnType = "Landroid/graphics/Path;",
    parameters = listOf("Landroid/text/Layout;"),
)

