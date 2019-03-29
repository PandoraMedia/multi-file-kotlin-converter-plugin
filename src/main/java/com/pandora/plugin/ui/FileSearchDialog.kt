/*
 * Copyright 2019 Pandora Media, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See accompanying LICENSE file or you may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pandora.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.SystemInfo
import com.pandora.plugin.options.IntComparison
import com.pandora.plugin.options.PanelOption
import com.pandora.plugin.options.SearchOptions
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagLayout
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*
import javax.swing.text.DocumentFilter
import javax.swing.text.PlainDocument
import javax.swing.text.NumberFormatter
import java.text.NumberFormat



class FileSearchDialog(project: Project,
                       private val message: String,
                       @Nls(capitalization = Nls.Capitalization.Title) title: String,
                       val checkboxText: String,
                       val checked: Boolean,
                       val checkboxEnabled: Boolean
) : DialogWrapper(project) {
    private lateinit var commitCheckBox: JCheckBox
    private lateinit var regexCheckBox: JCheckBox
    private lateinit var regexInput: JTextField
    private lateinit var lineCountCheckBox: JCheckBox
    private lateinit var lineCountCombo: ComboBox<IntComparison>
    private lateinit var lineCountInput: ComboBox<Int>

    init {
        this.title = title
        if (!SystemInfo.isMac) {
            setButtonsAlignment(SwingConstants.CENTER)
        }

        init()
    }

    private val commit
        get() = commitCheckBox.isSelected

    private val regexText
        get() = if (regexCheckBox.isSelected) regexInput.text else null

    private val lineCount
        get() = (if (lineCountCheckBox.isSelected) lineCountInput.selectedItem as Int? else null) ?: -1

    private val lineCountFunction: (Int, Int) -> Boolean
        get() = if (lineCountCheckBox.isSelected) (lineCountCombo.selectedItem as IntComparison).function else { _, _ -> true }

    private fun createTextComponent(str: String): JComponent {
        val textLabel = JLabel(str)
        textLabel.ui = MultiLineLabelUI()
        textLabel.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
        return textLabel
    }

    override fun createCenterPanel(): JComponent? {
        val messagePanel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP or VerticalFlowLayout.LEFT, true, false))

        messagePanel.add(createTextComponent(message))
        val regexPanel = JPanel(GridBagLayout())
        regexCheckBox = JCheckBox()
        regexCheckBox.text = "Regex"
        regexCheckBox.isSelected = lastRegexUsed
        regexCheckBox.isEnabled = true
        regexPanel.add(regexCheckBox, PanelOption.WRAP.constraints)

        regexInput = JTextField()
        regexInput.text = lastRegex
        regexPanel.add(regexInput, PanelOption.FILL.constraints)
        messagePanel.add(regexPanel)

        val lineCountPanel = JPanel(GridBagLayout())
        lineCountCheckBox = JCheckBox()
        lineCountCheckBox.text = "Line Count"
        lineCountCheckBox.isSelected = lastSizeUsed
        lineCountCheckBox.isEnabled = true
        lineCountPanel.add(lineCountCheckBox, PanelOption.WRAP.constraints)

        lineCountCombo = ComboBox(IntComparison.values())
        lineCountCombo.selectedItem = lastSizeType
        lineCountPanel.add(lineCountCombo, PanelOption.WRAP.constraints)

        lineCountInput = ComboBox((0..10000).toList().toTypedArray())
        lineCountInput.selectedItem = lastSize
        lineCountPanel.add(lineCountInput, PanelOption.FILL.constraints)

        messagePanel.add(lineCountPanel)

        commitCheckBox = JCheckBox()
        commitCheckBox.text = checkboxText
        commitCheckBox.isSelected = checked
        commitCheckBox.isEnabled = checkboxEnabled

        messagePanel.add(commitCheckBox)

        return messagePanel
    }


    companion object {
        private var lastRegexUsed: Boolean = false
        private var lastRegex: String = ""
        private var lastSizeUsed: Boolean = false
        private var lastSize: Int = 0
        private var lastSizeType: IntComparison = IntComparison.LESS_THAN

        fun showSearchDialog(project: Project,
                             message: String,
                             @Nls(capitalization = Nls.Capitalization.Title) title: String,
                             checkboxText: String,
                             checked: Boolean,
                             checkboxEnabled: Boolean): SearchOptions? {
            val dialog = FileSearchDialog(
                    project = project,
                    message = message,
                    title = title,
                    checkboxText = checkboxText,
                    checked = checked,
                    checkboxEnabled = checkboxEnabled)
            dialog.show()
            lastRegexUsed = dialog.regexCheckBox.isSelected
            lastRegex = dialog.regexInput.text
            lastSizeUsed = dialog.lineCountCheckBox.isSelected
            lastSize = dialog.lineCountInput.selectedItem as Int? ?: -1
            lastSizeType = dialog.lineCountCombo.selectedItem as IntComparison

            return if (dialog.isOK) SearchOptions(dialog.commit, dialog.regexText, dialog.lineCount, dialog.lineCountFunction) else null
        }
    }
}