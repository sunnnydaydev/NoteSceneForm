# HelloSceneForm

# 执行运行时检查并创建运行时视图

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

最简单的方法是直接使用ArFragment，系统自动处理了ArCore的一些必要东西：

- Google Play Services for AR 的检测，安装，更新。
- 相机权限的检测，申请。
- 自动处理ar会话管理。

若是不想使用默认的UI或者默认的设置我们可以有两种方式来改变：

（1）创建ArFragment或者BaseArFragment的子类（一般这种毕竟手动处理了权限等问题）

```kotlin
/**
 * Create by SunnyDay /11/15 14:54:12
 * 自定义的arFragment，进行一些方法的重写验证测试。
 */
class CustomFragment : ArFragment() {

    private val permissions =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)

    /**
     * 是否为ar必备，为ar必备才去申请危险权限、初始化session等操作。一般设置为true。
     * */
    override fun isArRequired(): Boolean {
        return super.isArRequired()
    }

    /**
     * 这个方法可以重写申请一些其他的权限，但系统会默认申请camera权限，且camera为必备权限。
     *
     * 我们额外加的权限为非必备权限，用户拒绝了仍然可以进入app，camera权限用户拒绝时系统会弹窗提示到设置页面。
     * */
    override fun getAdditionalPermissions(): Array<String> {
        return permissions
    }

    /**
     * 这个是异常处理的，在这里可以处理arcore回调过来的异常case，若不想more
     * */
    override fun handleSessionException(sessionException: UnavailableException?) {
        super.handleSessionException(sessionException)
    }

    /**
     * 重要方法：一般自定义fragment重写这个方法，进行一些必要的配置。
     * below：Face 3D mesh is enabled.
     * */
    override fun getSessionConfiguration(session: Session?): Config {
        return Config(session).apply {
            augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        }
    }

    /**
     * 重要方法：配置一些feature时重写这个方法。
     * 如下使用前摄像头
     * */
    override fun getSessionFeatures(): MutableSet<Session.Feature> {
        return EnumSet.of(Session.Feature.FRONT_CAMERA)
    }

    /**
     * 可重写默认的ar界面
     * */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val frameLayout = super.onCreateView(inflater, container, savedInstanceState) as? FrameLayout
        //隐藏指示物
        planeDiscoveryController.hide()
        //指示物布局置空
        planeDiscoveryController.setInstructionView(null)
        return  frameLayout
    }

}
```


（2）直接使用或扩展ArSceneView（这个需要手动处理版本检查，arcore录像等问题）

ArFragment内部的布局默认只有一个子View也就是ArSceneView，执行完必要的检测后
ArFragment就会做如下事情：

（1）将会话中相机图像渲染到ArSceneView上

（2）呈现内置SceneForm用户体验动画，向用户展示应如何移动手机以激活 AR 体验

（3）使用默认的 PlaneRenderer检测到"突出"显示内容(PlaneRenderer为ArSceneView的一个成员)

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

（3）使用ModelRenderable类进行构建可渲染对象

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

# 打造场景

ArSceneView的父类SceneView有一个成员Scene，Scene是场景。

场景是树状数据结构，上面有要呈现的虚拟对象的节点。我们可以吧可渲染对象直接附加到节点上~

也可将节点添加到其他节点，以形成父子关系。如果某个节点是另一个节点的子级，则该节点会随着其父级而移动、 旋转和缩放，就像
在身体移动时手臂会如何移动。一个节点可以有多个子级，但只能有一个父级，从而形成一种树状结构。这种结构称为场景图。

每一帧，Sceneform 都会从相机的视角（由 ARCore 运动跟踪指导）渲染场景图。可以通过监听触摸和手势事件、针对节点执行命中
测试以及放置锚点来与场景互动。

如下是ARFragment提供的一个事件监听，当用户在场景图中点击时这个方法就会回调：
```kotlin
        aRFragment?.setOnTapArPlaneListener { hitResult: HitResult, 
                                              plane: Plane?, 
                                              motionEvent: MotionEvent? ->

        }
```
###### 1、OnTapArPlaneListener涉及api

(1)HitResult

点击结果，常见的api如下

- com.google.ar.core.Anchor createAnchor():在点击位置创建一个锚点
- float getDistance()：返回照相机镜头到点击位置的距离，单位meter。

（2）Plane

描述现实世界的一些信息

（3）MotionEvent

手指在屏幕事件的触摸封装

###### 2、Node & Scene 相关科普

- Node为节点的意思

- Scene为场景的意思

- 整个场景图由多个节点组成

- 可以通过aRFragment?.arSceneView?.scene来获取Scene对象。并且Scene是NodeParent的子类:

```java
public class Scene extends NodeParent {
    ...
}
```

- 可以为节点指定父节点,也可为节点指定可渲染对象

```java
// 指定父节点
 public void setParent(@Nullable NodeParent var1) {
    
 }
 //指定可渲染对象
public void setRenderable(@Nullable Renderable var1) {
    
}
```

- 特殊的节点Anchor，这是一个锚点节点，创建也很简单

```kotlin
            // 通过hitResult可直接获取点击位置的锚点
            val anchor: Anchor = hitResult.createAnchor()
            // 根据锚点直接生成一个锚点节点
            val anchorNode = AnchorNode(anchor)
```

- 特殊的节点TransformableNode，这个节点支持选中、旋转、平移、缩放等手势。

```kotlin
            // 创建一个TransformableNode节点
            val andy = TransformableNode(aRFragment?.transformationSystem)
            // 设置父节点
            andy.setParent(anchorNode)
            // 设置可渲染对象
            andy.renderable = andyRenderable
            // 设置选中
            andy.select()
```


###### 3、用户交互

当用户在屏幕点击时会生成一个3d机器人，机器人支持平移、放大、缩小等手势~

```kotlin
        aRFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (andyRenderable == null) {
                return@setOnTapArPlaneListener
            }
            // Create the Anchor.
            val anchor: Anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(aRFragment?.arSceneView?.scene)

            // Create the transformable andy and add it to the anchor.
            val andy = TransformableNode(aRFragment?.transformationSystem)
            andy.setParent(anchorNode)
            andy.renderable = andyRenderable
            andy.select()
        }
```

# 小结

- 了解到ArFragment默认处理了异常、权限、Arcore会话，把相机图像实时渲染到屏幕
- 了解到了还可自定义ArFragment或者使用ArSceneView来实现自定义效果
- 了解到了几种可渲染对象（ViewRenderable和ModelRenderable）
- 了解到了Scene的概念，节点的概念
- 了解到了几种节点AnchorNode、TransformableNode。

