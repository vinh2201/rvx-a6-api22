package app.revanced.rvxa6.patches.youtube.misc.share

import app.revanced.rvxa6.patches.youtube.utils.resourceid.bottomSheetRecyclerView
import app.revanced.rvxa6.util.fingerprint.legacyFingerprint
import app.revanced.rvxa6.util.getReference
import app.revanced.rvxa6.util.indexOfFirstInstruction
import app.revanced.rvxa6.util.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal val bottomSheetRecyclerViewFingerprint = legacyFingerprint(
    name = "bottomSheetRecyclerViewFingerprint",
    returnType = "Lj${'$'}/util/Optional;",
    accessFlags = AccessFlags.PROTECTED or AccessFlags.FINAL,
    parameters = emptyList(),
    literals = listOf(bottomSheetRecyclerView),
)

internal val updateShareSheetCommandFingerprint = legacyFingerprint(
    name = "updateShareSheetCommandFingerprint",
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L", "Ljava/util/Map;"),
    opcodes = listOf(
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT
    ),
    customFingerprint = { method, _ ->
        method.indexOfFirstInstruction {
            opcode == Opcode.SGET_OBJECT &&
                    getReference<FieldReference>()?.name == "updateShareSheetCommand"
        } >= 0
    }
)
