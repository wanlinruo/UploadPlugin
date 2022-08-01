# UploadPlugin
## 一、Background
Gradle 插件，封装 Maven 上传  

目前实现的功能点：  
1、封装 `maven-publish` 插件，提供`UploadInfo`闭包块  
2、主制品支持`Jar`和`AAR`等类型上传  
3、副产物支持源码上传、依赖管理`pom`文件上传

## 二、Usage

### 2.1、settings.gradle
```
pluginManagement {
    repositories {
        ...
        maven { url 'https://www.wanlinruo.com/nexus/repository/maven-public/' }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == 'com.wanlinruo.plugin.upload') {
                useModule("com.wanlinruo.plugin:upload:0.0.64-SNAPSHOT")
            }
        }
    }
}
```
### 2.2、build.gradle
```
plugins {
    ...
    id 'com.wanlinruo.plugin.upload' version '0.0.64-SNAPSHOT' apply false
}
```

### 2.3、build.gradle（module）
```
plugins {
    ...
    id 'com.wanlinruo.plugin.upload'
}

uploadInfo {
    version = "xxx"
    artifactId = "xxx"
    groupId = "xxx"
}
```

## 三、Compatible
插件是基于`maven-publish`进行开发的，所以agp版本需要最低支持在3.6.3

## 四、License
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)