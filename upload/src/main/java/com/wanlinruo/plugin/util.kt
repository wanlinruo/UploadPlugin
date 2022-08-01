@file:Suppress("UnstableApiUsage")

package com.wanlinruo.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar

/**
 *  author : wanlinruo
 *  date : 2022/7/13 18:17
 *  contact : wanlinruo@163.com
 *  description :
 */
fun isEmpty(str: String): Boolean {
    return str.trim() == ""
}

fun isAndroidOrAndroidLibrary(project: Project): Boolean {
    return project.plugins.hasPlugin(AppPlugin::class.java)
            || project.plugins.hasPlugin(LibraryPlugin::class.java)
}

fun getBuildType(project: Project, isAndroid: Boolean): String {
    var result = ""
    if (isAndroid) {
        (project.extensions.getByName("android") as BaseExtension).buildTypes.forEach {
            // 区分变体，优先release
            if (it.name == "release") {
                return "release"
            }
            if (it.name == "debug") {
                result = "debug"
            }
        }
    } else {
        result = "jar"
    }
    return result
}

fun createSourceCodeJar(project: Project, isAndroid: Boolean): Jar {
    return project.tasks.create("sourceJar", Jar::class.java) {
        val sourceSetDirs = if (isAndroid) {
            ((project.extensions.getByType(LibraryExtension::class.java))
                .sourceSets.getByName("main").java as DefaultAndroidSourceDirectorySet)
                .apply {
                    filter.include("**/*.kt")
                }.srcDirs
        } else {
            (project.extensions.getByName("sourceSets") as SourceSetContainer)
                .getByName("main").allSource.srcDirs
        }
        it.archiveClassifier.set("sources")
        it.from(sourceSetDirs)
    }
}

fun handleDependency(pom: MavenPom, isRemove: Boolean) {
    pom.withXml { xml ->
        val root = xml.asNode()
        val dependenciesNode = root.get("dependencies") as NodeList
        println("dependenciesNode: ${dependenciesNode.text()}")
        // 由于from会帮我们自动收集依赖，所以这里只处理不需要依赖的情况
        if (isRemove) dependenciesNode.removeAll { true }
    }
}

fun addDependencies(target: Project, pom: MavenPom) {
    pom.withXml { xml ->
        val root = xml.asNode()
        val dependenciesNode = root.appendNode("dependencies")
        target.configurations.findByName("compile")
            ?.let { addDependency(dependenciesNode, it, "compile") }
        target.configurations.findByName("api")
            ?.let { addDependency(dependenciesNode, it, "compile") }
        target.configurations.findByName("implementation")
            ?.let { addDependency(dependenciesNode, it, "runtime") }
        target.configurations.findByName("compileOnly")
            ?.let { addDependency(dependenciesNode, it, "provided") }
        target.configurations.findByName("runtimeOnly")
            ?.let { addDependency(dependenciesNode, it, "runtime") }
        target.configurations.findByName("provided")
            ?.let { addDependency(dependenciesNode, it, "provided") }
        target.configurations.findByName("apk")
            ?.let { addDependency(dependenciesNode, it, "runtime") }
    }
}

fun addDependency(dependenciesNode: Node, configuration: Configuration, scope: String) {
    // 非compile都不录入
    if (scope != "compile") return
    // 需要找出依赖项中的版本信息
    configuration.dependencies.forEach {
        // 排除不合规的依赖
        if (it.group == null || it.version == null || it.name == "unspecified") return
        var group = it.group
        var name = it.name
        var version = it.version
        // 兼容逻辑，本地project的情况
        if (it is ProjectDependency) {
            val subDepUploadInfo =
                it.dependencyProject.extensions.create("uploadInfo", UploadInfo::class.java)
            group = subDepUploadInfo.groupId
            name = subDepUploadInfo.artifactId
            version = subDepUploadInfo.version
        }
        // 添加到依赖的node中
        val dependencyNode = dependenciesNode.appendNode("dependency")
        dependencyNode.appendNode("groupId", group)
        dependencyNode.appendNode("artifactId", name)
        dependencyNode.appendNode("version", version)
        dependencyNode.appendNode("scope", scope)
    }
}