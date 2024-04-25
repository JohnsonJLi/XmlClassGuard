package com.xml.guard.tasks

import com.xml.guard.entensions.GuardExtension
import com.xml.guard.utils.allDependencyAndroidProjects
import com.xml.guard.utils.resDir
import net.coobird.thumbnailator.Thumbnails
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.inject.Inject

open class CompressAndResizeTask @Inject constructor(
    guardExtension: GuardExtension
) : DefaultTask() {

    init {
        group = "Image Processing"
        description = "Compresses and resizes images in a specified directory"
    }

    private val guard: GuardExtension = guardExtension

    @TaskAction
    fun execute() {
        val androidProjects = allDependencyAndroidProjects()
        androidProjects.forEach { itP ->
            compressAndResize(itP.resDir().toPath())
            guard.flavor?.let {
                compressAndResize(itP.resDir(flavor = it).toPath())
            }
        }
    }

    private fun compressAndResize(projectDir: Path?) {
        if (!Files.notExists(projectDir) && Files.isDirectory(projectDir)) {
            println("XmlInsertTask:> projectDir: ${projectDir}")
            try {
                Files.walk(projectDir)
                    .filter {
                        val fileName = it.fileName.toString()
                        Files.isDirectory(it) && (fileName.startsWith("drawable")|| fileName.startsWith("mipmap") )
                    }
                    .forEach { insertDir ->
                        println("XmlInsertTask:> compressDir : ${insertDir} >f  ${insertDir.fileName}")
                        insertDir.toFile().listFiles { file -> file.isFile && (file.extension == "jpg" || file.extension == "png" || file.extension == "webp") }?.forEach { inputFile ->
                            val tempFilePath = File("${inputFile.parent}/${inputFile.name}_temp.${inputFile.extension}")

                            Thumbnails.of(inputFile.absolutePath)
                                .scale(0.999) // 缩小图片尺寸到原尺寸的50%
                                .outputQuality(0.98) // 设置压缩质量为80%
                                .toFile(tempFilePath)

                            println("Processed image: ${inputFile.name}")
                            Files.move(tempFilePath.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


}