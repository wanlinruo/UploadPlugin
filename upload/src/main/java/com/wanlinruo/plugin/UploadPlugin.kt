package com.wanlinruo.plugin

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
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

        // 确认组件类型
        val isAndroid = isAndroidOrAndroidLibrary(target)

        // 设置跳过规则
        if (isAndroid) if (target.plugins.hasPlugin(AppPlugin::class.java)) return

        // 确认组件的buildType
        val buildType = getBuildType(target)

        // 集成MavenPublishPlugin
        if (!target.plugins.hasPlugin(MavenPublishPlugin::class.java)) {
            target.plugins.apply(MavenPublishPlugin::class.java)
        }

        // 确认UploadInfo闭包块
        val info = target.extensions.create("uploadInfo", UploadInfo::class.java)

        // 在全部配置完成后，执行task之前的回调
        target.afterEvaluate { project ->

            // 检测UploadInfo
            if (isEmpty(info.groupId))
                throw IllegalArgumentException("the groupId of uploadInfo must not be empty")
            if (isEmpty(info.artifactId))
                throw IllegalArgumentException("the artifactId of uploadInfo must not be empty")
            if (isEmpty(info.version))
                throw IllegalArgumentException("the version of uploadInfo must not be empty")

            // 集成maven流程
            project.extensions.configure(PublishingExtension::class.java) { publishing ->
                // 准备凭证
                publishing.repositories.maven {
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
                publishing.publications.register("maven", MavenPublication::class.java).get()
                    .apply {
                        // 设置产物
                        if (isAndroid) {
                            from(project.components.getByName(buildType) as SoftwareComponent)
                        } else {
                            from(project.components.getByName("java") as SoftwareComponent)
                        }
                        // 设置版本信息
                        groupId = info.groupId
                        artifactId = info.artifactId
                        version = info.version
                        // 设置源码
                        if (info.sourceCode)
                            artifact(createSourceCodeJar(target, isAndroid))
                        // 设置依赖管理
                        pom { it.description.set("Upload AAR,the buildType is: $buildType") }
                        if (info.hasPomDepend)
                            handleDependency(target, pom)
                    }
            }

            // 创建task
            target.tasks
                .register("upload")
                .get()
                .apply {
                    if (isAndroid) {
                        if (buildType == "release") {
                            dependsOn("assembleRelease")
                        } else if (buildType == "debug") {
                            dependsOn("assembleDebug")
                        }
                    }
                }
                .dependsOn("publishMavenPublicationToMavenRepository")
                .apply {
                    group = "wanlinruo"
                }
                .doLast {
                    println("Upload success !")
                }
        }
    }
}