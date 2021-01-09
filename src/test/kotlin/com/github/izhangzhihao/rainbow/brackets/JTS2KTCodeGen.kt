package com.github.izhangzhihao.rainbow.brackets

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.PostprocessReformattingAspect
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.VfsTestUtil.deleteFile
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.jetbrains.kotlin.idea.j2k.IdeaJavaToKotlinServices
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.j2k.ConverterSettings
import org.jetbrains.kotlin.nj2k.NewJavaToKotlinConverter
import org.jetbrains.kotlin.nj2k.postProcessing.NewJ2kPostProcessor
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.util.*


class JTS2KTCodeGen : LightJavaCodeInsightFixtureTestCase() {

    fun addFile(file: File, dirName: String?): VirtualFile {
        return addFile(FileUtil.loadFile(file, true), file.name, dirName)
    }

    fun addFile(text: String, fileName: String, dirName: String?): VirtualFile {
        return runWriteAction {
            val root = LightPlatformTestCase.getSourceRoot()!!
            val virtualDir = dirName?.let {
                root.findChild(it) ?: root.createChildDirectory(null, it)
            } ?: root
            val virtualFile = virtualDir.createChildData(null, fileName)
            virtualFile.getOutputStream(null)!!.writer().use { it.write(text) }
            virtualFile
        }
    }

    fun listf(directoryName: String?, files: MutableList<File?>) {
        val directory = File(directoryName)

        // Get all files from a directory.
        val fList: Array<File> = directory.listFiles()
        for (file in fList) {
            if (file.isFile) {
                files.add(file)
            } else if (file.isDirectory) {
                listf(file.path, files)
            }
        }
    }

    fun testGen() {
        val psiManager = PsiManager.getInstance(project)
        val inputDir = "j2k/input/"
        val outputDir = "j2k/output/"
//        val filesToConvert = File(baseDir).listFiles { f, name -> name.endsWith(".java") }!!
        val start = mutableListOf<File?>()
        listf(inputDir, start)
        val filesToConvert = start.filterNotNull().filter { it.name.endsWith(".java") }

        assert(filesToConvert.isNotEmpty())

        val psiFilesToConvert = filesToConvert.map { javaFile ->
            val virtualFile = addFile(javaFile, "test")
            psiManager.findFile(virtualFile) as PsiJavaFile
        }

        val converter =
            NewJavaToKotlinConverter(project, module, ConverterSettings.defaultSettings, IdeaJavaToKotlinServices)

        val (results, _) =
            WriteCommandAction.runWriteCommandAction(project, Computable {
                PostprocessReformattingAspect.getInstance(project).doPostponedFormatting()
                return@Computable converter.filesToKotlin(psiFilesToConvert, NewJ2kPostProcessor())
            })

        fun expectedResultFile(i: Int) = File(filesToConvert[i].path.replace(".java", ".kt").replace(inputDir, outputDir))

        psiFilesToConvert.forEachIndexed { i, javaFile ->
            deleteFile(javaFile.virtualFile)
            val virtualFile = addFile(results[i], expectedResultFile(i).name, "j2k")
            val ktFile = psiManager.findFile(virtualFile) as KtFile

            val file = expectedResultFile(i)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            file.createNewFile()

            file.bufferedWriter().use { out ->
                out.write(ktFile.text)
            }
        }
    }
}