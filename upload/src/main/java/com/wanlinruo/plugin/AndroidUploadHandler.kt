package com.wanlinruo.plugin

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.maven.MavenPublication

class AndroidUploadHandler : UploadPlugin() {

    private var variant: String = "release"

    override fun isSkip(target: Project): Boolean {
        // 跳过直接可运行模块
        return target.plugins.hasPlugin("com.android.application")
    }

    override fun getAndroidVariant(target: Project) {
        // variant = debug/release
        (target.extensions.getByType(LibraryExtension::class.java)).buildTypes.forEach {
            println("当前有variant：$it")
            if (it.name == "debug")
                variant = "debug"
            if (it.name == "release")
                variant = "release"
        }
    }

    override fun executeAndroidArtifact(project: Project, mavenPublication: MavenPublication) {
        mavenPublication.apply {
            from(project.components.getByName(variant) as SoftwareComponent)
        }
    }
}