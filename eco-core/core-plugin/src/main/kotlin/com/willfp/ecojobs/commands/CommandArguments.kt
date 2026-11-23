package com.willfp.ecojobs.commands

import wiki.ecohub.core.command.CommandContext
import wiki.ecohub.ui.text.StringUtils

/**
 * Send the lang message [key] with [replacements], without translating placeholders in it first.
 */
internal fun CommandContext<*>.replyWithoutPlaceholders(key: String, vararg replacements: Pair<String, String>) {
    val message = plugin.langYml.getMessage(key, StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)

    sender.sendMessage(
        replacements.fold(message) { replaced, (placeholder, value) ->
            replaced.replace(oldValue = "%$placeholder%", newValue = value)
        }
    )
}
