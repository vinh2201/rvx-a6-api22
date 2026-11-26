package app.revanced.patches.youtube.layout.shortcut

import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.patch.PatchList.HIDE_SHORTCUTS
import app.revanced.patches.youtube.utils.playservice.is_19_44_or_greater
import app.revanced.patches.youtube.utils.playservice.versionCheckPatch
import app.revanced.patches.youtube.utils.settings.ResourceUtils.addPreference
import app.revanced.patches.youtube.utils.settings.settingsPatch
import app.revanced.util.findElementByAttributeValueOrThrow
import org.w3c.dom.Element

@Suppress("unused")
val shortcutPatch = resourcePatch(
    name = HIDE_SHORTCUTS.patchName,
    description = HIDE_SHORTCUTS.summary,
    use = false
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(
        settingsPatch,
        versionCheckPatch
    )

    val explore = booleanOption(
        default = false,
        name = "Hide Explore",
        description = "Hide Explore from shortcuts.",
        required = true
    )

    val subscriptions = booleanOption(
        default = false,
        name = "Hide Subscriptions",
        description = "Hide Subscriptions from shortcuts.",
        required = true
    )

    val search = booleanOption(
        default = false,
        name = "Hide Search",
        description = "Hide Search from shortcuts.",
        required = true
    )

    val shorts = booleanOption(
        default = true,
        name = "Hide Shorts",
        description = "Hide Shorts from shortcuts.",
        required = true
    )

    apply {
        // PORT-NOTE: v22 options have no `key`; the key was the shortcut id, so it is spelled out here.
        var options = listOf(
            "subscriptions" to subscriptions,
            "search" to search,
            "shorts" to shorts
        )

        if (!is_19_44_or_greater) {
            options += "explore" to explore
        }

        options.forEach { (shortcutId, option) ->
            if (option.value == true) {
                document("res/xml/main_shortcuts.xml").use { document ->
                    val shortcuts = document.getElementsByTagName("shortcuts").item(0) as Element
                    val shortsItem = shortcuts.getElementsByTagName("shortcut")
                        .findElementByAttributeValueOrThrow(
                            "android:shortcutId",
                            "$shortcutId-shortcut"
                        )
                    shortsItem.parentNode.removeChild(shortsItem)
                }
            }
        }

        addPreference(HIDE_SHORTCUTS)

    }
}
