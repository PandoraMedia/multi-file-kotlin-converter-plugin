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
package com.pandora.plugin.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.project.guessProjectDir
import com.pandora.plugin.CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID
import com.pandora.plugin.anyJavaFileSelected
import com.pandora.plugin.logger
import com.pandora.plugin.writeCommitHistory

class ConvertSelectedFileToKotlinWithHistory : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectBase = project.guessProjectDir()

        try {
            val fileArray = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
            if (fileArray == null || fileArray.isEmpty()) {
                logger.info("No files selected.")
                return
            } else {
                fileArray.forEach { logger.info("Preparing to convert file: $it") }
            }

            if (!writeCommitHistory(project, projectBase, fileArray)) {
                return
            }

            val dataContext = DataContext { data ->
                when (data) {
                    PlatformDataKeys.VIRTUAL_FILE_ARRAY.name -> fileArray
                    else -> e.dataContext.getData(data)
                }
            }
            val overrideEvent = AnActionEvent(e.inputEvent, dataContext, e.place, e.presentation, e.actionManager, e.modifiers)
            ActionManager.getInstance().getAction(CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID)?.actionPerformed(overrideEvent)
        } catch (e: Exception) {
            logger.error("Problem running conversion plugin: ${e.message}\n${e.stackTrace.joinToString("\n")}\n----------")
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.anyJavaFileSelected()
    }
}
