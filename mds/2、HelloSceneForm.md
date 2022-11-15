# HelloSceneForm

# 执行运行时检查并创建运行时视图

最简单的方法是使用ArFragment，系统自动处理了ArCore的一些必要东西：

- Google Play Services for AR 的检测，安装，更新。
- 相机权限的检测，申请。
- 自动处理ar会话管理。

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ui.HelloSceneForm">

    <fragment
        android:id="@+id/arFragment"
        android:name="com.google.ar.sceneform.ux.ArFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>
```

若是不想使用默认的UI或者默认的设置我们可以有两种方式来改变
- 创建ArFragment或者BaseArFragment的子类（一般这种毕竟手动处理了权限等问题）
- 直接使用或扩展ArSceneView（这个需要手动处理版本检查，arcore录像等问题）

继续接着上面的默认的ArFragment讲起，ArFragment内部的布局默认只有一个子View也就是ArSceneView，执行完必要的检测后
ArFragment就会做如下事情：
1、将会话中相机图像渲染到ArSceneView上
2、呈现内置SceneForm用户体验动画，向用户展示应如何移动手机以激活 AR 体验
3、使用默认的 PlaneRenderer检测到突出显示内容（PlaneRenderer为ArSceneView的一个成员）

```kotlin
  // 获取ArSceneView，在fragment中
   arSceneView
  // 获取ArCore session对象
   arSceneView.session
```

# 创建可渲染对象

Renderable是一种3D模型，由网格、材料和纹理组成。模型可放置在场景中的任何位置。

Renderable是一个抽象类，其有两个子实现类ViewRenderable和ModelRenderable。

那么如何创建Renderable呢，通常有三种方式：

###### 1、通过 Android widget 创建

这里借助ViewRenderable这个类，这个类在3d中渲染出一个2d卡片，同时还具备轻触卡片交互的功能。

![图片](https://developers.google.cn/static/sceneform/images/view-renderable.jpg)

很简单，我们只需要提供一个布局或者一个view即可~

```xml
<!-- test.xml-->
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
   android:id="@+id/planetInfoCard"
   android:layout_width="wrap_content"
   android:layout_height="wrap_content"
   android:layout_weight="1"
   android:background="@drawable/rounded_bg"
   android:gravity="center"
   android:orientation="vertical"
   android:padding="6dp"
   android:text="Test"
   android:textAlignment="center" />
```
```kotlin
ViewRenderable
    .builder()
    .setView(this,R.layout.test)
    .build()
    .thenAccept {
        //ViewRenderable 上下文 it
    }
```
build方法会返回 CompletableFuture对象。该对象在单独的线程上构建，回调函数在主线程上执行。

###### 2、通过3D素材资源创建

这个需要借助ModelRenderable类，这个类可根据一些3d资源文件，来创建一个可渲染对象。

Sceneform 提供了一些工具和插件，可用于将 3D 资源文件 (OBJ、CocoaPods、glTF) 转换为 Sceneform 二进制资源 (SFB)，
然后这些文件可以内置到 ModelRenderable中。

（1）首先吧资源文件放到app目录下

（2）app/build.gradle 生命资源目录

```groovy
//asset(String modelPath, String materialPath, String sfaPath, String sfbPath)
sceneform.asset('sampledata/models/andy.obj',
        'default',
        'sampledata/models/andy.sfa',
        'src/main/res/raw/andy')
```

（2）使用ModelRenderable类进行构建可渲染对象

```kotlin
        ModelRenderable.builder()
            .setSource(this, R.raw.andy)
            .build()
            .thenAccept {
                //ModelRenderable 上下文
            }
            .exceptionally(
                Function<Throwable, Void?> {

                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                        .show()
                    null
                })
```
此时编译时编译器会在res/raw下生成andy.sfb文件。

###### 3、运行时创建

如上的andy.obj文件，编译器在编译时根据这些文件转化为了sfb文件，而存储为 glTF 或 glb 文件的 3D 模型可以在运行时加载，
而无需进行转换。

为了使用运行时素材资源加载功能，您需要在 app/build.gradle 中添加对素材资源库的依赖关系：

```groovy
  dependencies {
     implementation 'com.google.ar.sceneform:assets:1.15.0'
  }
```

```kotlin

        // gltf 服务器模型地址
        val url =
            "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf"
        // 1、创建renderableSource对象
        val renderableSource = RenderableSource.builder().setSource(
            this,
            Uri.parse(url),
            RenderableSource.SourceType.GLTF2
        )
            .setScale(0.5f)  // Scale the original model to 50%.
            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
            .build()
        // 2、传递参数创建可渲染对象
        ModelRenderable.builder()
            .setSource(this, renderableSource)
            .setRegistryId(url)
            .build()
            .thenAccept {
                //ModelRenderable 上下文
            }
            .exceptionally(
                Function<Throwable, Void?> {
                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                        .show()
                    null
                })

```

###### 4、基于基本图形api创建

使用ShapeFactory和MaterialFactory可以创建立方体、球体和圆柱体等简单形状，让您可以使用简单的形状和材料创建可渲染对象。

![](https://developers.google.cn/static/sceneform/images/shape-renderable.jpg)

```kotlin
        //直接获取一个可渲染对象
        MaterialFactory.makeOpaqueWithColor(this, Color(Color.RED))
            .thenAccept { material: Material? ->
                val modelRenderable =
                    ShapeFactory.makeSphere(0.1f, Vector3(0.0f, 0.15f, 0.0f), material)
            }
```