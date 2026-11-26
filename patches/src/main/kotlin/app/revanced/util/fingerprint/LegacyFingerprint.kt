package app.revanced.util.fingerprint

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableClassDef
import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstruction
import app.revanced.util.injectLiteralInstructionViewCall
import app.revanced.util.parametersEqual
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.util.MethodUtil

/*
 * PORT-NOTE (patcher v21 -> v22):
 *
 * Patcher v22 replaced the old `Fingerprint`/`Match` classes with the composing/declarative matcher DSL.
 * The ~600 fingerprints of this bundle were written for the old semantics, so instead of rewriting each one
 * this file re-implements the old semantics on top of `BytecodePatchContext.classDefs`:
 *
 *  - accessFlags: exact equality.
 *  - returnType / parameters: prefix match (same as v21 `returns()` / `parameters()`).
 *  - strings: every string must be a *substring* of a distinct const-string in the method,
 *    `stringMatches` are reported in instruction order.
 *  - opcodes: pattern with `null` wildcards and an optional fuzzy threshold,
 *    `patternMatch.endIndex` is the INCLUSIVE index of the last matched instruction.
 *  - the first match in `classDefs` order wins.
 *
 * The call sites (`xFingerprint.methodOrThrow()`, `matchOrThrow().patternMatch!!.startIndex`, ...) are unchanged.
 */

private val String.exception
    get() = PatchException("Failed to resolve $this")

/**
 * Result of a successful [Fingerprint] match.
 */
class Match(
    private val context: BytecodePatchContext,
    val originalClassDef: ClassDef,
    val originalMethod: Method,
    val patternMatch: PatternMatch?,
    val stringMatches: List<StringMatch>?,
) {
    /** The mutable class that declares the matched method. */
    val classDef: MutableClassDef by lazy {
        context.classDefs.getOrReplaceMutable(context.classDefs[originalClassDef.type] ?: originalClassDef)
    }

    /** The mutable matched method. */
    val method: MutableMethod by lazy {
        classDef.methods.first { MethodUtil.methodSignaturesMatch(it, originalMethod) }
    }

    class PatternMatch(
        val startIndex: Int,
        /** INCLUSIVE index of the last instruction of the pattern. */
        val endIndex: Int,
    )

    class StringMatch(
        val string: String,
        val index: Int,
    )
}

/**
 * A fingerprint with the matching semantics of patcher v21.
 */
class Fingerprint internal constructor(
    private val accessFlags: Int?,
    private val returnType: String?,
    private val parameters: List<String>?,
    private val opcodes: List<Opcode?>?,
    private val strings: List<String>?,
    private val literals: List<Long>?,
    private val custom: ((method: Method, classDef: ClassDef) -> Boolean)?,
    private val fuzzyPatternScanThreshold: Int,
) {
    // Cache of the last successful match. Tied to the context so a second patching run in the same JVM
    // (e.g. the Manager patching several apps) never sees a stale match.
    private var cachedContext: BytecodePatchContext? = null
    private var cachedMatch: Match? = null

    /**
     * Finds the first match in all classes.
     */
    fun matchOrNull(context: BytecodePatchContext): Match? {
        if (cachedContext === context) return cachedMatch

        for (classDef in context.classDefs) {
            val match = matchOrNull(context, classDef)
            if (match != null) {
                cachedContext = context
                cachedMatch = match
                return match
            }
        }

        return null
    }

    /**
     * Finds the first match inside the methods of a single class.
     */
    fun matchOrNull(context: BytecodePatchContext, classDef: ClassDef): Match? {
        for (method in classDef.methods) {
            val match = matchOrNull(context, method, classDef)
            if (match != null) return match
        }

        return null
    }

    private fun matchOrNull(context: BytecodePatchContext, method: Method, classDef: ClassDef): Match? {
        if (accessFlags != null && accessFlags != method.accessFlags) return null
        if (returnType != null && !method.returnType.startsWith(returnType)) return null
        if (parameters != null && !parametersEqual(parameters, method.parameterTypes)) return null

        val needsImplementation = opcodes != null || strings != null || !literals.isNullOrEmpty()
        val implementation = method.implementation
        if (needsImplementation && implementation == null) return null

        if (custom != null && !custom.invoke(method, classDef)) return null

        var patternMatch: Match.PatternMatch? = null
        if (opcodes != null && opcodes.isNotEmpty()) {
            val instructionOpcodes = implementation!!.instructions.map { it.opcode }
            patternMatch = patternMatchOrNull(instructionOpcodes, opcodes) ?: return null
        }

        var stringMatches: List<Match.StringMatch>? = null
        if (strings != null) {
            val remaining = strings.toMutableList()
            val found = mutableListOf<Match.StringMatch>()

            implementation!!.instructions.forEachIndexed { index, instruction ->
                if (instruction.opcode != Opcode.CONST_STRING && instruction.opcode != Opcode.CONST_STRING_JUMBO) {
                    return@forEachIndexed
                }
                val string = ((instruction as ReferenceInstruction).reference as StringReference).string
                val i = remaining.indexOfFirst { string.contains(it) }
                if (i < 0) return@forEachIndexed

                found.add(Match.StringMatch(string, index))
                remaining.removeAt(i)
            }
            if (remaining.isNotEmpty()) return null

            stringMatches = found
        }

        if (literals != null) {
            for (literal in literals) {
                if (!method.containsLiteralInstruction(literal)) return null
            }
        }

        return Match(context, classDef, method, patternMatch, stringMatches)
    }

    private fun patternMatchOrNull(
        instructionOpcodes: List<Opcode>,
        pattern: List<Opcode?>,
    ): Match.PatternMatch? {
        val instructionLength = instructionOpcodes.size
        val patternLength = pattern.size
        if (instructionLength < patternLength) return null

        for (index in 0 until instructionLength) {
            var patternIndex = 0
            var threshold = fuzzyPatternScanThreshold

            while (index + patternIndex < instructionLength) {
                val originalOpcode = instructionOpcodes[index + patternIndex]
                val patternOpcode = pattern[patternIndex]

                if (patternOpcode != null && patternOpcode != originalOpcode) {
                    // Reaching the maximum threshold (0) means the pattern does not match here.
                    if (threshold-- == 0) break
                }

                if (++patternIndex < patternLength) continue

                return Match.PatternMatch(index, index + patternLength - 1)
            }
        }

        return null
    }
}

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.resolvable(): Boolean =
    second.matchOrNull(context) != null

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.definingClassOrThrow(): String =
    second.matchOrNull(context)?.originalClassDef?.type ?: throw first.exception

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.matchOrThrow(): Match =
    second.matchOrNull(context) ?: throw first.exception

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.matchOrThrow(parentFingerprint: Pair<String, Fingerprint>): Match {
    val parentMatch = parentFingerprint.second.matchOrNull(context)
        ?: throw parentFingerprint.first.exception
    val parentClassDef = context.classDefs[parentMatch.originalClassDef.type] ?: parentMatch.originalClassDef
    return second.matchOrNull(context, parentClassDef)
        ?: throw first.exception
}

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.matchOrNull(): Match? =
    second.matchOrNull(context)

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.matchOrNull(parentFingerprint: Pair<String, Fingerprint>): Match? {
    val parentMatch = parentFingerprint.second.matchOrNull(context) ?: return null
    val parentClassDef = context.classDefs[parentMatch.originalClassDef.type] ?: parentMatch.originalClassDef
    return second.matchOrNull(context, parentClassDef)
}

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.methodOrNull(): MutableMethod? =
    second.matchOrNull(context)?.method

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.methodOrThrow(): MutableMethod =
    matchOrThrow().method

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.methodOrThrow(parentFingerprint: Pair<String, Fingerprint>): MutableMethod =
    matchOrThrow(parentFingerprint).method

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.originalMethodOrThrow(): Method =
    matchOrThrow().originalMethod

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.originalMethodOrThrow(parentFingerprint: Pair<String, Fingerprint>): Method =
    matchOrThrow(parentFingerprint).originalMethod

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.mutableClassOrThrow(): MutableClassDef =
    matchOrThrow().classDef

context(context: BytecodePatchContext)
internal fun Pair<String, Fingerprint>.methodCall() =
    methodOrThrow().methodCall()

internal fun MutableMethod.methodCall(): String {
    var methodCall = "$definingClass->$name("
    for (i in 0 until parameters.size) {
        methodCall += parameterTypes[i]
    }
    methodCall += ")$returnType"
    return methodCall
}

context(context: BytecodePatchContext)
fun Pair<String, Fingerprint>.injectLiteralInstructionBooleanCall(
    literal: Long,
    descriptor: String
) {
    methodOrThrow().apply {
        val literalIndex = indexOfFirstLiteralInstruction(literal)
        val index = indexOfFirstInstructionOrThrow(literalIndex, Opcode.MOVE_RESULT)
        val register = getInstruction<OneRegisterInstruction>(index).registerA

        val smaliInstruction =
            if (descriptor.startsWith("0x")) """
                const/16 v$register, $descriptor
                """
            else if (descriptor.endsWith("(Z)Z")) """
                invoke-static/range { v$register .. v$register }, $descriptor
                move-result v$register
                """
            else """
                invoke-static {}, $descriptor
                move-result v$register
                """

        addInstructions(
            index + 1,
            smaliInstruction
        )
    }
}

context(context: BytecodePatchContext)
fun Pair<String, Fingerprint>.injectLiteralInstructionViewCall(
    literal: Long,
    smaliInstruction: String
) {
    val method = methodOrThrow()
    method.injectLiteralInstructionViewCall(literal, smaliInstruction)
}

internal fun legacyFingerprint(
    name: String,
    fuzzyPatternScanThreshold: Int = 0,
    accessFlags: Int? = null,
    returnType: String? = null,
    parameters: List<String>? = null,
    opcodes: List<Opcode?>? = null,
    strings: List<String>? = null,
    literals: List<Long>? = null,
    customFingerprint: ((methodDef: Method, classDef: ClassDef) -> Boolean)? = null,
) = Pair(
    name,
    Fingerprint(
        accessFlags = accessFlags,
        returnType = returnType,
        parameters = parameters,
        opcodes = opcodes,
        strings = strings,
        literals = literals,
        custom = customFingerprint,
        fuzzyPatternScanThreshold = fuzzyPatternScanThreshold,
    )
)
