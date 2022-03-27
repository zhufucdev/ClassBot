package com.zhufucdev.data.homework

import com.zhufucdev.Plugin
import java.io.File
import kotlin.io.path.Path

data class Homework(val info: HomeworkManifest, val files: List<File>, val revision: Revision) {
    fun handIn(files: List<File>) = Homework(info, files, revision)
    fun revise(revision: Revision) = Homework(info, files, revision)

    companion object {
        val fileContainer: File get() = Path(Plugin.dataFolder.path, "homework_files").toFile()
    }
}
