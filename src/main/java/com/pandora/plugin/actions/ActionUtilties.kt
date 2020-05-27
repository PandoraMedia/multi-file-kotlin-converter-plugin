package com.pandora.plugin.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.pandora.plugin.JAVA_EXTENSION

/**
 * Provides an overridden [DataContext] using the provided [fileArray]
 */
fun AnActionEvent.dataContext(fileArray: Array<VirtualFile>): DataContext = DataContext { data ->
    when (data) {
        PlatformDataKeys.VIRTUAL_FILE_ARRAY.name -> fileArray
        else -> dataContext.getData(data)
    }
}

/**
 * Searches the directory tree of a given [VirtualFile] for .java files that that can be converted
 */
fun VirtualFile.findMatchingChildren(matcher: (VirtualFile) -> Boolean): Array<VirtualFile> {
    val result = mutableListOf<VirtualFile>()
    VfsUtilCore.visitChildrenRecursively(this, object : VirtualFileVisitor<Unit>() {
        override fun visitFile(file: VirtualFile): Boolean {
            if (file.canConvert && matcher(file)) {
                result.add(file)
            }
            return true
        }
    })
    return result.toTypedArray()
}

private val VirtualFile.canConvert: Boolean
    get() = extension == JAVA_EXTENSION && isWritable && !isDirectory && !path.contains("/build/")
