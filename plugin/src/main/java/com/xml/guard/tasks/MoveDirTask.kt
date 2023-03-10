package com.xml.guard.tasks

import com.xml.guard.entensions.GuardExtension
import com.xml.guard.utils.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * User: ljx
 * Date: 2022/2/25
 * Time: 19:06
 */
open class MoveDirTask @Inject constructor(
    private val guardExtension: GuardExtension
) : DefaultTask() {

    init {
        group = "guard"
    }

    private var configFile: File? = null

    @TaskAction
    fun execute() {
        val moveFile = guardExtension.moveDir
        if (moveFile.isEmpty()) return
        configFile = guardExtension.assetsConfigPath?.let { project.assetsPath(it) }
        val androidProjects = allDependencyAndroidProjects()
        androidProjects.forEach { it.moveDir(moveFile) }
    }

    private fun Project.moveDir(moveFile: Map<String, String>) {
        val manifestPackage = findPackage()  //查找清单文件里的package属性值
        // 1、替换manifest文件 、layout/navigation目录下的文件、Java、Kt文件
        val listFiles = resDir().listFiles { _, name ->
            //过滤res目录下的layout、navigation目录
            name.startsWith("layout") || name.startsWith("navigation")
        }?.toMutableList() ?: mutableListOf()
        listFiles.add(manifestFile())


        try {
            if (!guardExtension.flavor.isNullOrEmpty()) {
                resDir(flavor = guardExtension.flavor!!).listFiles { _, name ->
                    //过滤res目录下的layout、navigation目录
                    name.startsWith("layout") || name.startsWith("navigation")
                }?.toMutableList()?.let {
                    listFiles.addAll(it)
                }
                javaDirs(flavor = guardExtension.flavor!!).let {
                    listFiles.addAll(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        listFiles.addAll(javaDirs())
        files(listFiles).asFileTree.forEach {
            it.replaceText(moveFile, manifestPackage)
        }

        // 2、开始移动目录
        moveFile.forEach { (oldPath, newPath) ->
            for (oldDir in javaDirs(oldPath.replace(".", File.separator))) {
                if (!oldDir.exists()) {
                    continue
                }
                if (oldPath == manifestPackage) {
                    //包名目录下的直接子类移动位置，需要重新手动导入R类及BuildConfig类(如果有用到的话)
                    oldDir.listFiles { f -> !f.isDirectory }?.forEach { file ->
                        file.insertImportXxxIfAbsent(oldPath)
                    }
                }
                copy {
                    it.from(oldDir)
                    it.into(javaDir(newPath.replace(".", File.separator), oldDir.absolutePath))
                }
                delete(oldDir)
            }
            try {
                if (!guardExtension.flavor.isNullOrEmpty()) {
                    for (oldDir in javaDirs(oldPath.replace(".", File.separator), flavor = guardExtension.flavor!!)) {
                        if (!oldDir.exists()) {
                            continue
                        }
                        if (oldPath == manifestPackage) {
                            //包名目录下的直接子类移动位置，需要重新手动导入R类及BuildConfig类(如果有用到的话)
                            oldDir.listFiles { f -> !f.isDirectory }?.forEach { file ->
                                file.insertImportXxxIfAbsent(oldPath)
                            }
                        }
                        copy {
                            it.from(oldDir)
                            it.into(javaDir(newPath.replace(".", File.separator), oldDir.absolutePath, flavor = guardExtension.flavor!!))
                        }
                        delete(oldDir)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun File.replaceText(map: Map<String, String>, manifestPackage: String?) {
        var replaceText = readText()
        map.forEach { (oldPath, newPath) ->
            replaceText = if (name == "AndroidManifest.xml" && oldPath == manifestPackage) {
                replaceText.replaceWords("$oldPath.", "$newPath.")
                    .replaceWords("""android:name=".""", """android:name="${newPath}.""")
            } else {
                configFile?.let { file ->
                    var readText = file.readText()
                    readText = FileUtils.replaceText(file, readText, oldPath, newPath)
                    file.writeText(readText)
                }
                replaceText.replaceWords(oldPath, newPath)
            }
            if (name.endsWith(".kt") || name.endsWith(".java")) {
                /*
                 移动目录时，manifest里的package属性不会更改
                 上面代码会将将R、BuildConfig等类路径替换掉，所以这里需要还原回去
                 */
                replaceText = replaceText.replaceWords("$newPath.R", "$oldPath.R")
                    .replaceWords("$newPath.BuildConfig", "$oldPath.BuildConfig")
                    .replaceWords("$newPath.databinding", "$oldPath.databinding")
            }
        }
        writeText(replaceText)
    }

}