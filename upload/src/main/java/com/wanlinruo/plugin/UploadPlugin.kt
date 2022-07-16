package com.wanlinruo.plugin

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.internal.DefaultPublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import java.net.URI

/**
 *  author : wanlinruo
 *  date : 2022/7/12
 *  contact : wanlinruo@163.com
 *  description :
 */
class UploadPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        println("===========UploadPlugin===========")
        // 跳过直接可运行模块
        if (target.plugins.hasPlugin(AppPlugin::class.java)) return

        // 集成MavenPublishPlugin
        if (!target.plugins.hasPlugin(MavenPublishPlugin::class.java)) {
            target.plugins.apply(MavenPublishPlugin::class.java)
            target.extensions
                .create("gradlePlugin", GradlePluginDevelopmentExtension::class.java)
                .plugins.create("UploadPlugin")
                .apply {
                    id = "com.wanlinruo.plugin.upload"
                    implementationClass = "com.wanlinruo.plugin.UploadPlugin"
                }
        }

        // 检测UploadInfo
        val info = target.extensions.create("uploadInfo", UploadInfo::class.java)
        if (isEmpty(info.groupId))
            throw IllegalArgumentException("the groupId of uploadInfo must not be empty")
        if (isEmpty(info.artifactId))
            throw IllegalArgumentException("the artifactId of uploadInfo must not be empty")
        if (isEmpty(info.version))
            throw IllegalArgumentException("the version of uploadInfo must not be empty")

        // 在全部配置完成后，执行task之前的回调
        target.afterEvaluate { project ->
            project.extensions.create("publishing", DefaultPublishingExtension::class.java).apply {
                // 准备凭证
                repositories.maven {
                    it.credentials.username = info.userName
                    it.credentials.password = info.password
                    // 区分上传目标地址
                    if (info.version.endsWith("-LOCAL")) {
                        it.url = project.repositories.mavenLocal().url
                    } else if (info.version.endsWith("-SNAPSHOT")) {
                        it.url = URI.create(info.snapshotUrl)
                    } else {
                        it.url = URI.create(info.releaseUrl)
                    }
                }
                // 准备信息
                publications.create("maven", MavenPublication::class.java).apply {
                    groupId = info.groupId
                    artifactId = info.artifactId
                    version = info.version
                    if (info.sourceCode)
                        artifact(createSourceCodeJar(target))
                    if (info.hasPomDepend)
                        handleDependency(target, pom)
                }
            }
            // 创建task
            val publishRealTask = "publishMavenPublicationToMavenRepository"
            project.task(publishRealTask)
            println("Upload success !")
        }
    }
}