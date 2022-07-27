package com.wanlinruo.plugin

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class AndroidUploadHandler : UploadPlugin() {

    override fun isSkip(target: Project): Boolean {
        // 跳过直接可运行模块
        return target.plugins.hasPlugin("com.android.application")
    }

    override fun executeAndroidArtifact(project: Project, mavenPublication: MavenPublication) {
        mavenPublication.apply {
            if (project.components.findByName("debug") != null) {
                from(project.components.getByName("debug"))
            }
            if (project.components.findByName("release") != null) {
                from(project.components.getByName("release"))
            }
        }
    }
}