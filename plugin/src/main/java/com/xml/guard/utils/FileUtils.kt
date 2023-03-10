package com.xml.guard.utils

import java.io.File

object FileUtils {


     fun replaceText(
        rawFile: File,
        rawText: String,
        rawPath: String,
        obfuscatePath: String,
    ): String {
        val rawIndex = rawPath.lastIndexOf(".")
        val rawPackage = rawPath.substring(0, rawIndex)
        val rawName = rawPath.substring(rawIndex + 1)

        val obfuscateIndex = obfuscatePath.lastIndexOf(".")
        val obfuscatePackage = obfuscatePath.substring(0, obfuscateIndex)
        val obfuscateName = obfuscatePath.substring(obfuscateIndex + 1)

        var replaceText = rawText
        when {
            rawFile.absolutePath.removeSuffix().endsWith(obfuscatePath.replace(".", File.separator)) -> {
                //对于自己，替换package语句及类名即可
                replaceText = replaceText
                    .replaceWords("package $rawPackage", "package $obfuscatePackage")
                    .replaceWords(rawPath, obfuscatePath)
                    .replaceWords(rawName, obfuscateName)
            }
            rawFile.parent.endsWith(obfuscatePackage.replace(".", File.separator)) -> {
                //同一包下的类，原则上替换类名即可，但考虑到会依赖同包下类的内部类，所以也需要替换包名+类名
                replaceText = replaceText.replaceWords(rawPath, obfuscatePath)  //替换{包名+类名}
                    .replaceWords(rawName, obfuscateName)
            }
            else -> {
                replaceText = replaceText.replaceWords(rawPath, obfuscatePath)  //替换{包名+类名}
                    .replaceWords("$rawPackage.*", "$obfuscatePackage.*")
                //替换成功或已替换
                if (replaceText != rawText || replaceText.contains("$obfuscatePackage.*")) {
                    //rawFile 文件内有引用 rawName 类，则需要替换类名
                    replaceText = replaceText.replaceWords(rawName, obfuscateName)
                }
            }
        }
        return replaceText
    }
}