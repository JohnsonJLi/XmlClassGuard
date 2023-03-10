package com.xml.guard.tasks

import com.android.build.gradle.BaseExtension
import com.xml.guard.entensions.GuardExtension
import com.xml.guard.utils.*
import groovy.xml.XmlParser
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
open class PackageChangeTask @Inject constructor(
    private val guardExtension: GuardExtension
) : DefaultTask() {

    init {
        group = "guard"
    }

    @TaskAction
    fun execute() {
        val packageExtension = guardExtension.packageChange
        if (packageExtension.isEmpty()) return
        val androidProjects = allDependencyAndroidProjects()
        androidProjects.forEach { it.changePackage(packageExtension) }
    }

    private fun Project.changePackage(map: Map<String, String>) {
        //1.修改 build.gradle namespace
        val namespace = modifyBuildGradleFile(map)
        //2.修改AndroidManifest.xml文件
        val pair = modifyManifestFile(map, namespace) ?: return
        val oldPackage = pair.first
        val oldSyntheticName = oldPackage.getSuffixName()
        println("oldSyntheticName : $oldSyntheticName")

        val newPackage = pair.second

        //3.修改 kt/java文件
        files("src/main/java").asFileTree.forEach { javaFile ->
            javaFile.readText()
                .replaceWords("$oldPackage.R", "$newPackage.R")
                .replaceWords("$oldPackage.BuildConfig", "$newPackage.BuildConfig")
                .replaceWords("$oldPackage.databinding", "$newPackage.databinding")
                .let { javaFile.writeText(it) }
        }

        try {
            if (!guardExtension.flavor.isNullOrEmpty()) {
                //3.修改 kt/java文件
                files("src/${guardExtension.flavor}/java").asFileTree.forEach { javaFile ->
                    javaFile.readText()
                        .replaceWords("$oldPackage.R", "$newPackage.R")
                        .replaceWords("$oldPackage.BuildConfig", "$newPackage.BuildConfig")
                        .replaceWords("$oldPackage.databinding", "$newPackage.databinding")
                        .replaceWords("kotlinx.android.synthetic.$oldSyntheticName", "kotlinx.android.synthetic.${guardExtension.flavor}")
                        .let { javaFile.writeText(it) }
                }

                //3.对旧包名下的直接子类，检测R类、BuildConfig类是否有用到，有的话，插入import语句
                javaDirs(oldPackage.replace(".", File.separator), flavor = guardExtension.flavor!!)
                    .forEach {
                        it.listFiles { f -> !f.isDirectory }
                            ?.forEach { file ->
                                file.insertImportXxxIfAbsent(newPackage)
                            }
                    }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        //3.对旧包名下的直接子类，检测R类、BuildConfig类是否有用到，有的话，插入import语句
        javaDirs(oldPackage.replace(".", File.separator))
            .forEach {
                it.listFiles { f -> !f.isDirectory }
                    ?.forEach { file ->
                        file.insertImportXxxIfAbsent(newPackage)
                    }
            }

    }

    //修复build.gradle文件的 namespace 语句，并返回namespace
    private fun Project.modifyBuildGradleFile(map: Map<String, String>): String? {
        if (AgpVersion.versionCompare("4.2.0") < 0) return null
        val namespace =
            (extensions.getByName("android") as BaseExtension).namespace ?: return null
        val newPackage = map[namespace] ?: return null
        buildFile.readText()
            .replace("namespace\\s+['\"]${namespace}['\"]".toRegex(), "namespace '$newPackage'")
            .let { buildFile.writeText(it) }
        return namespace
    }

    //修改AndroidManifest.xml文件，并返回新旧包名
    private fun Project.modifyManifestFile(
        map: Map<String, String>,
        namespace: String?
    ): Pair<String, String>? {
        val manifestFile = manifestFile()
        val oldPackage = namespace ?: manifestFile.findPackage() ?: return null
        val newPackage = map[oldPackage] ?: return null
        manifestFile.readText()
            .replaceWords("""package="$oldPackage"""", """package="$newPackage"""")
            .replaceWords("""android:name=".""", """android:name="$oldPackage.""")
            .let { manifestFile.writeText(it) }
        return Pair(oldPackage, newPackage)
    }

    private fun File.findPackage(): String? {
        val rootNode = XmlParser(false, false).parse(this)
        return rootNode.attribute("package")?.toString()
    }

}