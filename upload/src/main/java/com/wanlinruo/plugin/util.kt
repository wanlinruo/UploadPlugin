@file:Suppress("UnstableApiUsage")

package com.wanlinruo.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet
import org.gradle.api.Project
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

fun createSourceCodeJar(project: Project): Jar {
    return project.tasks.create("sourceJar", Jar::class.java) {
        val main = project.extensions.getByType(LibraryExtension::class.java)
            .sourceSets.getByName("main")
        it.archiveClassifier.set("sources")
        it.from(
            main.java.also { set -> set.include("**/*.kt") }.srcDirs,
            (main.kotlin as DefaultAndroidSourceDirectorySet).srcDirs
        )
    }
}