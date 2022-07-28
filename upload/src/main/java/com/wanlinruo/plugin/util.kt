@file:Suppress("UnstableApiUsage")

package com.wanlinruo.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import groovy.util.Node
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

fun getBuildType(project: Project): String {
    var result = ""
    (project.extensions.getByName("android") as BaseExtension).buildTypes.forEach {
        // 区分变体，优先release
        if (it.name == "release") {
            return "release"
        }
        if (it.name == "debug") {
            result = "debug"
        }
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

fun handleDependency(target: Project, pom: MavenPom) {
    pom.withXml { xml ->
        val root = xml.asNode()
        val dependenciesNode = root.appendNode("dependencies")
        target.configurations.forEach {
            if (it.name == "compile") addDependency(dependenciesNode, it, "compile")
            if (it.name == "api") addDependency(dependenciesNode, it, "compile")
            if (it.name == "implementation") addDependency(dependenciesNode, it, "runtime")
            if (it.name == "compileOnly") addDependency(dependenciesNode, it, "provided")
            if (it.name == "runtimeOnly") addDependency(dependenciesNode, it, "runtime")
            if (it.name == "provided") addDependency(dependenciesNode, it, "provided")
            if (it.name == "apk") addDependency(dependenciesNode, it, "runtime")
        }
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