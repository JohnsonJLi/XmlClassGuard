package com.xml.guard

import XmlInsertTask
import com.android.build.gradle.AppExtension
import com.xml.guard.entensions.GuardExtension
import com.xml.guard.entensions.VariantExt
import com.xml.guard.model.aabResGuard
import com.xml.guard.model.andResGuard
import com.xml.guard.tasks.*
import com.xml.guard.utils.AgpVersion
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * User: ljx
 * Date: 2022/2/25
 * Time: 19:03
 */
class XmlClassGuardPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        checkApplicationPlugin(project)
        println("XmlClassGuard version is $version, agpVersion=${AgpVersion.agpVersion}")
        val android = project.extensions.getByName("android") as AppExtension
//        if (!android.hasProperty("applicationVariants")) {
//            throw IllegalArgumentException("must apply this plugin after 'com.android.application'")
//        }
        val variantExt = project.extensions.create("xmlClassGuard", VariantExt::class.java, project.container(GuardExtension::class.java))
        project.afterEvaluate {
            android.applicationVariants.all { variant ->
                val variantName = variant.name

                val guardExtension = variantExt.variantConfig.findByName(variantName)
                if (guardExtension != null) {

                    val xmlInsertName = "${variantName}XmlInsert"
                    val compressAndResizeName = "${variantName}CompressAndResize"
                    val moveDir = "${variantName}MoveDir"
                    val packageChangeName = "${variantName}PackageChange"
                    val flavorXmlClassGuardName = "${variantName}FlavorXmlClassGuard"
                    val xmlClassGuardName = "${variantName}XmlClassGuard"
                    val name = project.name
                    println(variantName)
                    println("./gradlew :$name:$moveDir :$name:$packageChangeName :$name:$flavorXmlClassGuardName\n:$name:$xmlClassGuardName :$name:$xmlInsertName :$name:$compressAndResizeName")

                    project.tasks.create(xmlInsertName, XmlInsertTask::class.java, guardExtension)
                    project.tasks.create(compressAndResizeName, CompressAndResizeTask::class.java, guardExtension)
                    project.tasks.create(xmlClassGuardName, XmlClassGuardTask::class.java, guardExtension)
                    project.tasks.create(packageChangeName, PackageChangeTask::class.java, guardExtension)
                    project.tasks.create(moveDir, MoveDirTask::class.java, guardExtension)
                    project.tasks.create(flavorXmlClassGuardName, FlavorXmlClassGuardTask::class.java, guardExtension)

                    val variantNameCapitalize = variant.name.capitalize()
                    if (guardExtension.findAndConstraintReferencedIds) {
                        createAndFindConstraintReferencedIds(project, variantNameCapitalize)
                    }
                    if (guardExtension.findAabConstraintReferencedIds) {
                        createAabFindConstraintReferencedIds(project, variantNameCapitalize)
                    }
                }
            }
        }
    }

    private fun createAndFindConstraintReferencedIds(
        project: Project,
        variantName: String
    ) {
        val andResGuardTaskName = "resguard$variantName"
        val andResGuardTask = project.tasks.findByName(andResGuardTaskName)
            ?: throw GradleException("AndResGuard plugin required")
        val findConstraintReferencedIdsTaskName = "andFindConstraintReferencedIds"
        val findConstraintReferencedIdsTask =
            project.tasks.findByName(findConstraintReferencedIdsTaskName)
                ?: project.tasks.create(
                    findConstraintReferencedIdsTaskName,
                    FindConstraintReferencedIdsTask::class.java,
                    andResGuard
                )
        andResGuardTask.dependsOn(findConstraintReferencedIdsTask)
    }

    private fun createAabFindConstraintReferencedIds(
        project: Project,
        variantName: String
    ) {
        val aabResGuardTaskName = "aabresguard$variantName"
        val aabResGuardTask = project.tasks.findByName(aabResGuardTaskName)
            ?: throw GradleException("AabResGuard plugin required")
        val findConstraintReferencedIdsTaskName = "aabFindConstraintReferencedIds"
        val findConstraintReferencedIdsTask =
            project.tasks.findByName(findConstraintReferencedIdsTaskName)
                ?: project.tasks.create(
                    findConstraintReferencedIdsTaskName,
                    FindConstraintReferencedIdsTask::class.java,
                    aabResGuard
                )
        aabResGuardTask.dependsOn(findConstraintReferencedIdsTask)
    }

    private fun checkApplicationPlugin(project: Project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw GradleException("Android Application plugin required")
        }
    }
}