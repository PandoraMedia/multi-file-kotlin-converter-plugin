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
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.pandora.plugin.CONVERT_JAVA_TO_KOTLIN_PLUGIN_ID
import com.pandora.plugin.JAVA_EXTENSION
import com.pandora.plugin.logger
import com.pandora.plugin.options.SearchOptions
import com.pandora.plugin.ui.FileSearchDialog
import com.pandora.plugin.ui.MultiCheckboxDialog.Companion.showMultiCheckboxDialog
import com.pandora.plugin.writeCommitHistory
import java.util.*

/**
 * Custom action executing the following steps on each selected file(s):
 *
 * 0. Request user to enter list of files for conversion
 * 0. [Optional] Rename step in GIT
 * 0. [Optional] Simple file extension rename for GIT (`.java` to `.kt`)
 * 0. [Optional] Commit to GIT, with editable commit message
 * 0. [Optional] Rename file back to `.java`
 * 0. Use Native `ConvertJavaToKotlin` action to convert requested files.
 *
 * @see <a href="https://github.com/JetBrains/kotlin/blob/master/idea/src/org/jetbrains/kotlin/idea/actions/JavaToKotlinAction.kt">
 *     Link to 'ConvertJavaToKotlin' official source code</a>
 */
class SearchAndConvertFilesToKotlinWithHistory : AnAction() {

    // region Plugin implementation

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return
        val projectBase = project.baseDir

        try {
            val dialogResult = FileSearchDialog.showSearchDialog(project, "How to search for files:", "Search", "Automatically rename files in VCS", true, true)
                    ?: return
            val fileArray = verifyFiles(project, lineCountVerify(project, projectBase, dialogResult, regexVerify(projectBase, dialogResult.regex)))

            if (fileArray.isEmpty()) {
                logger.info("No files selected.")
                return
            } else {
                fileArray.forEach { logger.info("Preparing to convert file: $it") }
            }

            if (dialogResult.commit && !writeCommitHistory(project, projectBase, fileArray)) {
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
        e.presentation.isEnabled = true
    }

    // endregion

    // region Utility function

    private fun regexVerify(projectBase: VirtualFile, regex: String?): Array<VirtualFile>? {
        if (regex == null) return null
        val r = Regex.fromLiteral(regex)
        return findMatchingChildren(projectBase) { file -> r.containsMatchIn(file.name) }
    }

    private fun lineCountVerify(project: Project, projectBase: VirtualFile, searchOptions: SearchOptions, inputList: Array<VirtualFile>?): Array<VirtualFile>? {
        if (searchOptions.lineCount < 0) return inputList
        if (inputList == null) {
            val lambda = counter@{
                return@counter findMatchingChildren(projectBase) { file ->
                    val lines = LoadTextUtil.loadText(file).lines().count()
                    logger.info("Line count for $file: $lines")
                    searchOptions.countCompare(lines, searchOptions.lineCount)
                }
            }
            return ProgressManager.getInstance().runProcessWithProgressSynchronously<Array<VirtualFile>?, Exception>(lambda, "Scanning Files...", false, project)
        }
        val trimLambda = trim@{
            return@trim inputList.filter {
                val lines = LoadTextUtil.loadText(it).lines().count()
                logger.info("Line count for $it: $lines")
                searchOptions.countCompare(lines, searchOptions.lineCount)
            }.toTypedArray()
        }
        return ProgressManager.getInstance().runProcessWithProgressSynchronously<Array<VirtualFile>?, Exception>(trimLambda, "Scanning Files...", false, project)
    }

    private fun findMatchingChildren(projectBase: VirtualFile, matcher: (VirtualFile) -> Boolean): Array<VirtualFile> {
        val result = ArrayList<VirtualFile>()
        VfsUtilCore.visitChildrenRecursively(projectBase, object : VirtualFileVisitor<Unit>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (file.extension == JAVA_EXTENSION
                        && file.isWritable
                        && !file.isDirectory
                        && !file.path.contains("/build/")
                        && matcher(file))
                    result.add(file)
                return true
            }
        })

        return result.toTypedArray()
    }

    private fun verifyFiles(project: Project, files: Array<VirtualFile>?, formatter: (Any) -> String = { (it as VirtualFile).presentableName }): Array<VirtualFile> {
        if (files == null || files.isEmpty()) return emptyArray()
        return showMultiCheckboxDialog(*files, project = project, title = "Verify Files", itemFormatter = formatter).mapNotNull { it as? VirtualFile }.toTypedArray()
    }

    // endregion

}