package com.xml.guard.tasks

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi
import com.xml.guard.entensions.GuardExtension
import com.xml.guard.utils.allDependencyAndroidProjects
import com.xml.guard.utils.resDir
import net.coobird.thumbnailator.Thumbnails
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.plugins.jpeg.JPEGImageWriteParam
import javax.imageio.spi.IIORegistry
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
                        Files.isDirectory(it) && (fileName.startsWith("drawable")/* || fileName.startsWith("mipmap")*/)
                    }
                    .forEach { insertDir ->
                        println("XmlInsertTask:> compressDir : ${insertDir} >f  ${insertDir.fileName}")
                        insertDir.toFile().listFiles { file -> file.isFile && file.extension in listOf("jpg", "png"/*, "webp"*/) }?.forEach { inputFile ->
                            println("Processed image: ${inputFile.name}   >exists  ${inputFile.exists()}  >canRead  ${inputFile.canRead()}")
                            val tempFilePath = File("${inputFile.parent}/${inputFile.nameWithoutExtension}_temp.png")

                            when (inputFile.extension) {
                                "webp" -> {
                                    processWebP(inputFile, tempFilePath, 0.999f, 0.98f)
                                    Files.move(tempFilePath.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                                    inputFile.renameTo(File(inputFile.absolutePath.replace(".webp",".png")))
                                }
                                else -> {
                                    Thumbnails.of(inputFile.absolutePath)
                                        .scale(0.999)
                                        .outputQuality(0.98)
                                        .toFile(tempFilePath)
                                    if (tempFilePath.exists() && tempFilePath.canWrite()) {
                                        Files.move(tempFilePath.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                                    }
                                }
                            }


                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun processWebP(inputFile: File, tempFilePath: File, scale: Float, quality: Float) {

        println("Processed image absolutePath: ${inputFile.absolutePath}")
        // 注册WebP读取器
        val registry = IIORegistry.getDefaultInstance()
        registry.registerServiceProvider(WebPImageReaderSpi())

        // 获取WebP读取器并读取文件
        val readers = ImageIO.getImageReadersByFormatName("webp")
        if (readers.hasNext()) {
            val reader = readers.next()
            reader.setInput(ImageIO.createImageInputStream(inputFile))

            // 读取第一帧作为原始图像
            val originalImage = reader.read(0)

            // 计算缩放后的尺寸
            val scaledWidth = (originalImage.width * scale).toInt()
            val scaledHeight = (originalImage.height * scale).toInt()

            // 缩放图片
            val scaledImage = BufferedImage(scaledWidth, scaledHeight, originalImage.getType())
            val ato = AffineTransformOp(AffineTransform.getScaleInstance(scale.toDouble(), scale.toDouble()), AffineTransformOp.TYPE_BILINEAR)
            ato.filter(originalImage, scaledImage)

            // 创建WebP写入参数
            val writers = ImageIO.getImageWritersByFormatName("png")
            if (writers.hasNext()) {
                val writer = writers.next()
                val param = writer.getDefaultWriteParam()
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT)
                param.setCompressionQuality(quality)

                // 写入压缩后的WebP图片
                try {
                    FileOutputStream(tempFilePath).use { outputStream ->
                        writer.setOutput(ImageIO.createImageOutputStream(outputStream))
                        writer.write(null, IIOImage(scaledImage, null, null), param)
                    }
                } finally {
                    writer.dispose()
                }
            }

        } else {
            throw RuntimeException("No WebP reader found despite registering the WebP plugin.")
        }
    }

}