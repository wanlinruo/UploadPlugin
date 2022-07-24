package com.wanlinruo.plugin

import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.publish.maven.MavenPublication

class AndroidUploadHandler : UploadPlugin() {

    lateinit var variant: String

    override fun isSkip(target: Project): Boolean {
        // 跳过直接可运行模块
        return target.plugins.hasPlugin("com.android.application")
    }

    override fun executeAndroidAssemble(target: Project) {
        // variant = debug/release
        (target.extensions.getByName("android") as ExtraPropertiesExtension)
    }

    override fun executeAndroidArtifact(project: Project, mavenPublication: MavenPublication) {
        mavenPublication.apply {
            from(project.components.getByName(variant) as SoftwareComponent)
        }
    }
}