package com.wanlinruo.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class UploadPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        println("====================== UploadPlugin")
    }
}