package app.revanced.rvxa6.patches.youtube.utils.missingresources

import app.revanced.rvxa6.util.fingerprint.legacyFingerprint
import app.revanced.rvxa6.util.or
import com.android.tools.smali.dexlib2.AccessFlags

internal val navigationBarGetDrawableFingerprint = legacyFingerprint(
    name = "getDrawableFingerprint",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    returnType = "Landroid/graphics/drawable/Drawable;",
    parameters = listOf("Landroid/content/Context;", "I"),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "a"
        // && classDef.type == "Lzv;"
    }
)