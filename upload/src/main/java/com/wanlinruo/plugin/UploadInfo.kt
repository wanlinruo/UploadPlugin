package com.wanlinruo.plugin

/**
 *  author : wanlinruo
 *  date : 2022/7/13 11:16
 *  contact : wanlinruo@163.com
 *  description :
 */
data class UploadInfo(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val releaseUrl: String = "https://www.wanlinruo.com/repository/maven-releases/",
    val snapshotUrl: String = "https://www.wanlinruo.com/repository/maven-snapshots/",
    val userName: String = "uploader",
    val password: String = "uploader",
    val hasPomDepend: Boolean = true,// 是否包含依赖项
    val sourceCode: Boolean = true,// 是否包含源码
)
