package com.wanlinruo.plugin

import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.maven.MavenPublication

class JavaUploadHandler : UploadPlugin() {

    override fun isSkip(target: Project): Boolean {
        return super.isSkip(target)

    }

    override fun executeJavaArtifact(project: Project, mavenPublication: MavenPublication) {
        mavenPublication.apply {
            from(project.components.getByName("java") as SoftwareComponent)
        }
    }
}