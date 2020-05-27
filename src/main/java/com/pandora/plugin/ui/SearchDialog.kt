package com.pandora.plugin.ui

import org.jetbrains.annotations.Nls

data class SearchDialog(
    val message: String = "How to search for files:",
    @Nls(capitalization = Nls.Capitalization.Title) val title: String = "Search",
    val checkboxText: String = "Automatically rename files in VCS",
    val checked: Boolean = true,
    val checkboxEnabled: Boolean = true
)
