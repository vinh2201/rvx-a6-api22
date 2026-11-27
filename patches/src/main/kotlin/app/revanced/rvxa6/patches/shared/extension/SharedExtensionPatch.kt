package app.revanced.rvxa6.patches.shared.extension

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.rvxa6.patches.shared.extension.Constants.EXTENSION_PATCH_STATUS_CLASS_DESCRIPTOR
import app.revanced.rvxa6.patches.shared.extension.Constants.EXTENSION_UTILS_CLASS_DESCRIPTOR
import app.revanced.rvxa6.util.findMethodsOrThrow
import app.revanced.rvxa6.util.fingerprint.legacyFingerprint
import app.revanced.rvxa6.util.fingerprint.Fingerprint
import app.revanced.rvxa6.util.fingerprint.methodOrThrow
import app.revanced.rvxa6.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import java.util.jar.Manifest

/**
 * Name of the extension file (without `.rve`), the same as the folder name in `extensions/`.
 */
internal const val SHARED_EXTENSION_NAME = "rvxa6-shared"

fun sharedExtensionPatch(
    vararg hooks: ExtensionHook,
) = bytecodePatch(
    description = "sharedExtensionPatch"
) {
    extendWith("extensions/$SHARED_EXTENSION_NAME.rve")

    apply {
        if (classDefs[EXTENSION_UTILS_CLASS_DESCRIPTOR] == null) {
            throw PatchException(
                "Shared extension has not been merged yet. This patch can not succeed without merging it.",
            )
        }
        hooks.forEach { hook -> hook(EXTENSION_UTILS_CLASS_DESCRIPTOR) }
    }

    afterDependents {
        findMethodsOrThrow(EXTENSION_PATCH_STATUS_CLASS_DESCRIPTOR).apply {
            find { method -> method.name == "PatchedTime" }
                ?.replaceInstruction(
                    0,
                    "const-wide v0, ${System.currentTimeMillis()}L"
                )

            find { method -> method.name == "PatchVersion" }
                ?.apply {
                    val manifest = object {}
                        .javaClass
                        .classLoader
                        .getResources("META-INF/MANIFEST.MF")

                    while (manifest.hasMoreElements()) {
                        Manifest(manifest.nextElement().openStream())
                            .mainAttributes
                            .getValue("Version")
                            ?.let {
                                returnEarly(it)
                                return@afterDependents
                            }
                    }
                }
        }
    }
}

class ExtensionHook internal constructor(
    val fingerprint: Pair<String, Fingerprint>,
    private val insertIndexResolver: ((Method) -> Int),
    private val contextRegisterResolver: (Method) -> String,
) {
    context(context: BytecodePatchContext)
    operator fun invoke(extensionClassDescriptor: String) {
        val method = fingerprint.methodOrThrow()
        val insertIndex = insertIndexResolver(method)
        val contextRegister = contextRegisterResolver(method)

        method.addInstruction(
            insertIndex,
            "invoke-static/range { $contextRegister .. $contextRegister }, " +
                    "$extensionClassDescriptor->setContext(Landroid/content/Context;)V",
        )
    }
}

fun extensionHook(
    insertIndexResolver: ((Method) -> Int) = { 0 },
    contextRegisterResolver: (Method) -> String = { "p0" },
    accessFlags: Int? = null,
    returnType: String? = null,
    parameters: List<String>? = null,
    opcodes: List<Opcode?>? = null,
    strings: List<String>? = null,
    customFingerprint: ((methodDef: Method, classDef: ClassDef) -> Boolean)? = null,
) = ExtensionHook(
    legacyFingerprint(
        name = "extensionHookFingerprint",
        accessFlags = accessFlags,
        returnType = returnType,
        parameters = parameters,
        opcodes = opcodes,
        strings = strings,
        customFingerprint = customFingerprint,
    ),
    insertIndexResolver,
    contextRegisterResolver
)
