package com.wanlinruo.plugin

/**
 *  author : wanlinruo
 *  date : 2022/7/13 11:16
 *  contact : wanlinruo@163.com
 *  description :
 */
open class UploadInfo {
    var groupId: String = ""
    var artifactId: String = ""
    var version: String = ""
    var releaseUrl: String = "https://www.wanlinruo.com/nexus/repository/maven-releases/"
    var snapshotUrl: String = "https://www.wanlinruo.com/nexus/repository/maven-snapshots/"
    var userName: String = "uploader"
    var password: String = "uploader"
    var hasPomDepend: Boolean = true// 是否包含依赖项
    var sourceCode: Boolean = true// 是否包含源码

    override fun toString(): String {
        return "UploadInfo(groupId='$groupId', artifactId='$artifactId', version='$version', releaseUrl='$releaseUrl', snapshotUrl='$snapshotUrl', userName='$userName', password='$password', hasPomDepend=$hasPomDepend, sourceCode=$sourceCode)"
    }
}
