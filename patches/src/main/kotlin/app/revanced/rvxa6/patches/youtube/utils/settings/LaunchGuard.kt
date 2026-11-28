package app.revanced.rvxa6.patches.youtube.utils.settings

import app.revanced.patcher.patch.ResourcePatchContext
import org.w3c.dom.Element
import java.util.logging.Logger

/*
 * Launch guard: manifest-only tweaks that keep a patched APK from failing at start-up.
 *
 * Ported from the "crash prevention" manifest patches of Nai64Patches (Morphe):
 *   - Clearing Split Metadata            -> clearSplitMetadata
 *   - Repair Missing Component Export Flags -> repairExportFlags
 *   - Relax Required Libraries           -> relaxRequiredLibraries
 *   - Unmark Test Only                   -> unmarkTestOnly
 *   - Force Extract Native Libs          -> extractNativeLibs
 *   - Disable Heap Pointer Tagging       -> disableHeapPointerTagging
 *   - VM Safe Mode                       -> vmSafeMode
 *
 * It is NOT a patch of its own: it runs inside "Settings for YouTube (A6)", which almost every A6 patch depends on,
 * and every item can be switched on/off with an option of that patch (handy to find out which one fixes a crash).
 */
private const val NS_ANDROID = "http://schemas.android.com/apk/res/android"

// NOTE: attributes are written with setAttribute("android:...") like the rest of this repository does. That updates an
// attribute that already exists (whether the manifest DOM is namespace aware or not) instead of adding a duplicate.

private val logger = Logger.getLogger("LaunchGuard")

private val SPLIT_ROOT_ATTRIBUTES = listOf(
    "requiredSplitTypes",
    "splitTypes",
    "isSplitRequired",
    "isFeatureSplit",
    "split",
)

private val SPLIT_APPLICATION_ATTRIBUTES = listOf(
    "isSplitRequired",
)

private val SPLIT_METADATA_NAMES = setOf(
    "com.android.stamp.source",
    "com.android.stamp.type",
    "com.android.vending.splits",
    "com.android.vending.derived.apk.id",
    "com.android.dynamic.apk.fused.modules",
    "com.android.vending.splits.required",
)

private val EXPORT_COMPONENT_TAGS = listOf("activity", "activity-alias", "service", "receiver")

internal class LaunchGuardConfig(
    val clearSplitMetadata: Boolean,
    val repairExportFlags: Boolean,
    val relaxRequiredLibraries: Boolean,
    val unmarkTestOnly: Boolean,
    val extractNativeLibs: Boolean,
    val disableHeapPointerTagging: Boolean,
    val vmSafeMode: Boolean,
) {
    val anyEnabled
        get() = clearSplitMetadata || repairExportFlags || relaxRequiredLibraries || unmarkTestOnly ||
                extractNativeLibs || disableHeapPointerTagging || vmSafeMode
}

private fun Element.removeAndroidAttribute(name: String): Boolean {
    var removed = false
    if (hasAttributeNS(NS_ANDROID, name)) {
        removeAttributeNS(NS_ANDROID, name)
        removed = true
    }
    if (hasAttribute("android:$name")) {
        removeAttribute("android:$name")
        removed = true
    }
    // The un-prefixed form is used by the root `split` attribute.
    if (name == "split" && hasAttribute(name)) {
        removeAttribute(name)
        removed = true
    }
    return removed
}

private fun Element.hasAndroidAttribute(name: String) =
    hasAttributeNS(NS_ANDROID, name) || hasAttribute("android:$name")

internal fun ResourcePatchContext.applyLaunchGuard(config: LaunchGuardConfig) {
    if (!config.anyEnabled) return

    document("AndroidManifest.xml").use { manifest ->
        val root = manifest.documentElement
        val application = root.getElementsByTagName("application").item(0) as? Element

        if (config.clearSplitMetadata) {
            var removed = 0

            SPLIT_ROOT_ATTRIBUTES.forEach { if (root.removeAndroidAttribute(it)) removed++ }
            application?.let { app ->
                SPLIT_APPLICATION_ATTRIBUTES.forEach { if (app.removeAndroidAttribute(it)) removed++ }
            }

            val metadata = manifest.getElementsByTagName("meta-data")
            for (index in metadata.length - 1 downTo 0) {
                val element = metadata.item(index) as? Element ?: continue
                if (element.getAttributeNS(NS_ANDROID, "name") in SPLIT_METADATA_NAMES ||
                    element.getAttribute("android:name") in SPLIT_METADATA_NAMES
                ) {
                    element.parentNode?.removeChild(element)
                    removed++
                }
            }

            logger.info("Launch guard: removed $removed split-install manifest entries")
        }

        if (config.repairExportFlags) {
            // DEVIATION from the Morphe patch: that one writes "false" for every component that is not a launcher,
            // which would break deep links / share targets. Components that have an intent-filter and no explicit
            // flag were exported before Android 12, so "true" keeps the old behaviour and still installs on 12+.
            var repaired = 0
            for (tag in EXPORT_COMPONENT_TAGS) {
                val components = manifest.getElementsByTagName(tag)
                for (i in 0 until components.length) {
                    val component = components.item(i) as? Element ?: continue
                    if (component.hasAndroidAttribute("exported")) continue
                    if (component.getElementsByTagName("intent-filter").length == 0) continue

                    component.setAttribute("android:exported", "true")
                    repaired++
                }
            }
            logger.info("Launch guard: repaired $repaired missing android:exported flag(s)")
        }

        if (config.relaxRequiredLibraries) {
            val libraries = manifest.getElementsByTagName("uses-library")
            for (i in 0 until libraries.length) {
                (libraries.item(i) as? Element)?.setAttribute("android:required", "false")
            }
            logger.info("Launch guard: relaxed ${libraries.length} <uses-library> entries")
        }

        if (application == null) {
            logger.warning("Launch guard: no <application> element found, application attributes were skipped")
            return@use
        }

        if (config.unmarkTestOnly && application.removeAndroidAttribute("testOnly")) {
            logger.info("Launch guard: removed android:testOnly")
        }

        if (config.extractNativeLibs) {
            application.setAttribute("android:extractNativeLibs", "true")
            logger.info("Launch guard: android:extractNativeLibs=true")
        }

        if (config.disableHeapPointerTagging) {
            application.setAttribute("android:allowNativeHeapPointerTagging", "false")
            logger.info("Launch guard: android:allowNativeHeapPointerTagging=false")
        }

        if (config.vmSafeMode) {
            application.setAttribute("android:vmSafeMode", "true")
            logger.info("Launch guard: android:vmSafeMode=true")
        }
    }
}
