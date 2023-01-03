# 环境配置

###### 1、插件引入

project build.gradle中

```groovy
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        //sceneForm need this repo
        google()
        mavenLocal()
    }
    dependencies {
        //apply sceneForm plugin
        classpath 'com.google.ar.sceneform:plugin:1.17.1'
    }
}
plugins {
    id 'com.android.application' version '7.2.1' apply false
    id 'com.android.library' version '7.2.1' apply false
    id 'org.jetbrains.kotlin.android' version '1.6.10' apply false
}
task clean(type: Delete) {
    delete rootProject.buildDir
}
```
###### 2、依赖

app build.gradle中

```groovy
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'org.jetbrains.kotlin.android.extensions'
    // use sceneForm plugin
    id 'com.google.ar.sceneform.plugin'
}

android {
    defaultConfig {
        // SceneForm requires minSdkVersion >= 24.
        minSdkVersion 24
    }
    // SceneForm libraries use language constructs from Java 8.
    // Add these compile options if targeting minSdkVersion < 26.
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Provides ARCore Session and related resources.
    implementation 'com.google.ar:core:1.33.0'
    // Provides ArFragment, and other UX resources.
    implementation 'com.google.ar.sceneform.ux:sceneform-ux:1.17.1'
    // Alternatively, use ArSceneView without the UX dependency.
    implementation 'com.google.ar.sceneform:core:1.17.1'
}

```

###### 3、清单文件配置

```groovy
    //manifest 节点下
    <uses-permission android:name="android.permission.CAMERA" />
    //SceneForm requires OpenGL ES 3.0 or later. 
    <uses-feature android:glEsVersion="0x00030000" android:required="true" />
    //Indicates that app requires ARCore ("AR Required"). Ensures the app is
    //visible only in the Google Play Store on devices that support ARCore.
    //For "AR Optional" apps remove this line. 
    <uses-feature android:name="android.hardware.camera.ar" />
    // application 节点下
    <meta-data android:name="com.google.ar.core" android:value="required" />

```
