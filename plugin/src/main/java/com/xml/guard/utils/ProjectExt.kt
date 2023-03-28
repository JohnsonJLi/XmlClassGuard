package com.xml.guard.utils

import com.android.build.gradle.BaseExtension
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import java.io.File

/**
 * User: ljx
 * Date: 2022/3/6
 * Time: 22:54
 */

val whiteList = arrayListOf(
    "layout", "data", "variable", "import", "merge", "ViewStub", "include",
    "LinearLayout", "RelativeLayout", "FrameLayout", "AbsoluteLayout",
    "Button", "TextView", "View", "ImageView", "EditText", "ProgressBar",
    "androidx.constraintlayout.widget.ConstraintLayout",
    "androidx.core.widget.NestedScrollView",
    "androidx.constraintlayout.widget.Group",
    "androidx.constraintlayout.widget.Guideline",
    "androidx.appcompat.widget.Toolbar",
    "com.google.android.material.button.MaterialButton",
    "GridLayout", "GridView",
)

fun Project.findPackage(): String {
    if (AgpVersion.versionCompare("4.2.0") >= 0) {
        val namespace = (extensions.getByName("android") as BaseExtension).namespace
        if (namespace != null) {
            return namespace
        }
    }
    val rootNode = XmlParser(false, false).parse(manifestFile())
    return rootNode.attribute("package").toString()
}

fun Project.javaDir(path: String, lookPath: String, flavor: String = "main"): File {
    val javaDirs = javaDirs("", flavor)
    if (lookPath.isEmpty()) {
        return file("${javaDirs[0]}/$path")
    }
    for (dir in javaDirs) {
        if (lookPath.startsWith(dir.absolutePath)) {
            return file("$dir/$path")
        }
    }
    return file("${javaDirs[0]}/$path")
}

fun Project.javaDirs(path: String = "", flavor: String = "main"): List<File> {
    val sourceSet = (extensions.getByName("android") as BaseExtension).sourceSets
    val javaDirs = sourceSet.getByName(flavor).java.srcDirs
    return javaDirs.map { file("$it/$path") }
}

fun Project.assetsPath(path: String, flavor: String = "main"): File? {
    val sourceSet = (extensions.getByName("android") as BaseExtension).sourceSets
    val javaDirs = sourceSet.getByName(flavor).assets.srcDirs
    project.files(*javaDirs.toTypedArray()).asFileTree.forEach { file ->
        if (file.path.contains(path)) return file
    }
    return null
}

fun Project.resDir(path: String = "", flavor: String = "main"): File = file("src/$flavor/res/$path")

fun Project.manifestFile(): File = file("src/main/AndroidManifest.xml")

fun Project.javaTree(path: String = ""): ConfigurableFileTree = fileTree("src/main/java/$path")

fun Project.resTree(path: String = ""): ConfigurableFileTree = fileTree("src/main/res/$path")


//查找依赖的Android Project，也就是子 module，包括间接依赖的子 module
fun Project.findDependencyAndroidProject(
    projects: MutableList<Project>,
    names: List<String> = mutableListOf("api", "implementation")
) {
    names.forEach { name ->
        val dependencyProjects = configurations.getByName(name).dependencies
            .filterIsInstance<DefaultProjectDependency>()
            .filter { it.dependencyProject.isAndroidProject() }
            .map { it.dependencyProject }
        projects.addAll(dependencyProjects)
        dependencyProjects.forEach {
            it.findDependencyAndroidProject(projects, mutableListOf("api"))
        }
    }
}

fun Project.isAndroidProject() =
    plugins.hasPlugin("com.android.application")
            || plugins.hasPlugin("com.android.library")

//查找dir所在的Project，dir不存在，返回null
fun Project.findLocationProject(dir: String, flavor: String = "main"): Project? {
    val packageName = dir.replace(".", File.separator)
    val absoluteDirs = javaDirs(packageName, flavor = flavor)
    if (absoluteDirs.any { it.exists() }) {
        return this
    }
    val dependencyProjects = mutableListOf<Project>()
    findDependencyAndroidProject(dependencyProjects)
    dependencyProjects.forEach {
        val project = it.findLocationProject(dir, flavor = flavor)
        if (project != null) return project
    }
    return null
}

fun findClassByLayoutXml(text: String, classPaths: MutableList<String>) {
    val childrenList = XmlParser(false, false).parseText(text).breadthFirst()
    for (children in childrenList) {
        val childNode = children as? Node ?: continue
        val classPath = childNode.name().toString()
        if (classPath !in whiteList) {
            classPaths.add(classPath)
            val layoutManager = childNode.attribute("app:layoutManager")?.toString()
            if (layoutManager != null && !layoutManager.startsWith("androidx.recyclerview.widget.")) {
                classPaths.add(layoutManager)
            }
            val layoutBehavior = childNode.attribute("app:layout_behavior")?.toString()
            if (layoutBehavior != null && !layoutBehavior.startsWith("com.google.android.")) {
                classPaths.add(layoutBehavior)
            }
        }
    }
}

fun findClassByNavigationXml(text: String, classPaths: MutableList<String>) {
    val rootNode = XmlParser(false, false).parseText(text)
    for (children in rootNode.children()) {
        val childNode = children as? Node ?: continue
        val childName = childNode.name()
        if ("fragment" == childName) {
            val classPath = childNode.attribute("android:name").toString()
            classPaths.add(classPath)
        }
    }
}

//在manifest文件里，查找四大组件及Application，返回文件的package属性，即包名
fun findClassByManifest(text: String, classPaths: MutableList<String>, packageName: String) {
    val rootNode = XmlParser(false, false).parseText(text)
    val nodeList = rootNode.get("application") as? NodeList ?: return
    val applicationNode = nodeList.firstOrNull() as? Node ?: return
    val application = applicationNode.attribute("android:name")?.toString()
    if (application != null) {
        val classPath = if (application.startsWith(".")) packageName + application else application
        classPaths.add(classPath)
    }
    for (children in applicationNode.children()) {
        val childNode = children as? Node ?: continue
        val childName = childNode.name()
        if ("activity" == childName || "service" == childName ||
            "receiver" == childName || "provider" == childName
        ) {
            val name = childNode.attribute("android:name").toString()
            val classPath = if (name.startsWith(".")) packageName + name else name
            classPaths.add(classPath)
        }
    }
}