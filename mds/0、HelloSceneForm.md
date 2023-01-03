# HelloSceneForm

一切伟大的语言都是从HelloWorld开始，ARSceneForm入门我们也以一个HelloSceneForm程序来作为入门实例。HelloSceneForm这个demo官方已经为我们提供，我们可以直接clone来跑下效果：

初次安装AR程序系统会提示你下载必要的组件，组件下载完成后就可顺利进入app了。打开app首先会看到一个手好像握了一个充电宝在屏幕上不停摇摆，其实是提示你移动下手机扫描下附近的物体。

此时假如我们扫下地面，则会看到很多"小白点"，这个是ArFragment自带识别平面的UI，此时我们在屏幕上任意一点"点击"下，会发现屏幕上出现了一个Android机器人，这个机器人还可随意拖动。


熟悉下效果后我们就简单的认识下项目：

1、布局

很简单，直接使用sdk提供的ArFragment，使用这个Fragment可以很大简化代码，系统帮我们处理了权限申请，ar Session等配置。

2、Activity

```kotlin
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //1、检测手机的版本是否支持（安卓7.0，openGl3.0）
    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    //2、创建可渲染物（点击屏幕时3d Android 机器人）
    ModelRenderable.builder()
        .setSource(this, R.raw.andy)
        .build()
        .thenAccept(renderable -> andyRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });
      
    // 3、场景点击监听
    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (andyRenderable == null) {
            return;
          }

          // 3.1、点击处创建锚点
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          // 3.2、创建可变换的节点，并给节点指定父节点。
          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
          andy.setParent(anchorNode);
          andy.setRenderable(andyRenderable);
          andy.select();
        });
  }
```

小结

- 相机下采集的界面我们可以理解为场景。
- 在场景中点击的位置称为锚点，锚点可附加到场景中。
- 通过Renderable的子类我们可以为场景添加3d渲染物体





