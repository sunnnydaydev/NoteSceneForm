# 测量人的瞳距

思路很简单：

1、拿到瞳孔坐标

注意这里需要注意ArCore的AugmentedFace只提供了三个面部区域特征点（RegionType枚举定义），其余的特征点位置未进行枚举
实例的提供，我们需要根据文档提供的468人脸特征点图去自己计算。

2、计算空间距离

其他没啥坑，实现简单，看代码即可~ 

```kotlin
/**
 * use google arCore to get pupillary distance.
 * */
class PupillaryDistanceActivity : AppCompatActivity() {
    // test data
    private val tableVerticesWithTriangle = floatArrayOf(
        //第一个三角形
        -0.5f, -0.5f,
        0.5f, 0.5f,
        -0.5f, 0.5f,
        // 第二个三角形
        -0.5f, -0.5f,
        0.5f, -0.5f,
        0.5f, 0.5f,
        // 中间的分割线
        -0.5f, 0f,
        0.5f, 0f,
        // 两木追顶点
        0f, -0.25f,
        0f, 0.25f
    )
    private var vertexData = ByteBuffer
        .allocateDirect(tableVerticesWithTriangle.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(tableVerticesWithTriangle)

    companion object {
        private const val MIN_OPENGL_VERSION = 3.0
        private const val SQUARE = 2.0
    }

    private val faceNodeMap = mutableMapOf<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beforeSetContentView()
        setContentView(R.layout.activity_pupillary_distance)

        // test how to get value from floatBuffer. because of invoking vertexData.array failed directly
        // we can get index by invoking data by for loop
        for (i in 0 until vertexData.position()) {
            Timber.d("测试:${vertexData[i]}")
        }

        var renderable: Renderable? = null
        ViewRenderable
            .builder()
            .setView(this, R.layout.layout_pd_model)
            .build().thenAccept {
                renderable = it
            }


        val arFragment = supportFragmentManager.findFragmentById(R.id.pupil) as FaceArFragment?
        val arSceneView = arFragment?.arSceneView
        val scene = arSceneView?.scene

        arSceneView?.scene?.addOnUpdateListener {
            val faceList = arSceneView.session?.getAllTrackables(AugmentedFace::class.java)
            faceList?.forEach {

                if (!faceNodeMap.containsKey(it)) {
                    val faceNode = AugmentedFaceNode(it)
                    //  faceNode.setParent(scene) // 无模型时脸部是一块黑
                    faceNode.renderable = renderable
                    faceNodeMap[it] = faceNode
                }

                /**
                 * 发现：
                 *
                 *  一共1404个数据，应该是 3个分一组一共 468组 人体面部特征点。
                 *
                 * 数据如何分组？
                 *
                 *    组            1   2    3    4     ....   n
                 *
                 * index[0-1403)   2   5    8    11    ....   3n-1
                 *
                 * 拿空间坐标：（3n-3，3n-2，3n-1）
                 *
                 * 计算空间两点坐标距离：
                 *
                 *   勾股定理，具体参考photo/三点距离.png
                 *
                 *
                 *  瞳孔特征点取值（left，right）：
                 *
                 *
                 *   上三点
                 *
                 *  （1）第一组  (160,385)  结果59mm, 瞳孔在屏幕内时, 结果上下2mm波动。
                 *  （2）第二组  (159,386)  结果58mm, 瞳孔在屏幕内时, 结果上下3mm波动。
                 *  （3）第三组  (158,387)  结果58mm, 瞳孔在屏幕内时, 结果上下3mm波动。
                 *
                 *   下三点
                 *
                 *  （1）第一组  (144, 380)  结果140mm, 误差明显较大
                 *  （2）第二组  (145, 374)  结果80mm   误差明显较大
                 *  （3）第三组  (153, 373)  结果132mm  误差明显较大
                 *
                 *   其他点
                 *      27-257   44mm
                 *     163-381  121mm
                 *     154-390  119mm
                 *     161-384  106mm
                 *     159-385  54mm （少了9mm,有点大了）
                 *
                 *     160-387  68mm （多了5mm平均值可算入计算范围）
                 *     159-387  64mm  (多了1)
                 *
                 *
                 *
                 *   发现：使用第一组特征点相对来说精确点。
                 *
                 *
                 * */
                //
                Timber.d("所有数据：${it.meshVertices.position()}")

                //取瞳孔数据

                val leftVertexX = it.meshVertices[3 * 160 - 3].toDouble()
                val leftVertexY = it.meshVertices[3 * 160 - 2].toDouble()
                val leftVertexZ = it.meshVertices[3 * 160 - 1].toDouble()

                val rightVertexX = it.meshVertices[3 * 385 - 3].toDouble()
                val rightVertexY = it.meshVertices[3 * 385 - 2].toDouble()
                val rightVertexZ = it.meshVertices[3 * 385 - 1].toDouble()

                val leftVertexX2 = it.meshVertices[3 * 159 - 3].toDouble()
                val leftVertexY2 = it.meshVertices[3 * 159 - 2].toDouble()
                val leftVertexZ2 = it.meshVertices[3 * 159 - 1].toDouble()

                val rightVertexX2 = it.meshVertices[3 * 386 - 3].toDouble()
                val rightVertexY2 = it.meshVertices[3 * 386 - 2].toDouble()
                val rightVertexZ2 = it.meshVertices[3 * 386 - 1].toDouble()

                val leftVertexX3 = it.meshVertices[3 * 160 - 3].toDouble()
                val leftVertexY3 = it.meshVertices[3 * 160 - 2].toDouble()
                val leftVertexZ3 = it.meshVertices[3 * 160 - 1].toDouble()

                val rightVertexX3 = it.meshVertices[3 * 387 - 3].toDouble()
                val rightVertexY3 = it.meshVertices[3 * 387 - 2].toDouble()
                val rightVertexZ3 = it.meshVertices[3 * 387 - 1].toDouble()

                val leftVertexX4 = it.meshVertices[3 * 159 - 3].toDouble()
                val leftVertexY4 = it.meshVertices[3 * 159 - 2].toDouble()
                val leftVertexZ4 = it.meshVertices[3 * 159 - 1].toDouble()

                val rightVertexX4 = it.meshVertices[3 * 387 - 3].toDouble()
                val rightVertexY4 = it.meshVertices[3 * 387 - 2].toDouble()
                val rightVertexZ4 = it.meshVertices[3 * 387 - 1].toDouble()

                val leftVertexX5 = it.meshVertices[3 * 158 - 3].toDouble()
                val leftVertexY5 = it.meshVertices[3 * 158 - 2].toDouble()
                val leftVertexZ5 = it.meshVertices[3 * 158 - 1].toDouble()

                val rightVertexX5 = it.meshVertices[3 * 387 - 3].toDouble()
                val rightVertexY5 = it.meshVertices[3 * 387 - 2].toDouble()
                val rightVertexZ5 = it.meshVertices[3 * 387 - 1].toDouble()

//                Timber.d("leftVertex:($leftVertexX,$leftVertexY,$leftVertexZ)")
//                Timber.d("rightVertex:($rightVertexX,$rightVertexY,$rightVertexZ)")

                val result = (rightVertexX - leftVertexX).pow(SQUARE) +
                        (rightVertexY - leftVertexY).pow(SQUARE) +
                        (rightVertexZ - leftVertexZ).pow(SQUARE)

                val result2 = (rightVertexX2 - leftVertexX2).pow(SQUARE) +
                        (rightVertexY2 - leftVertexY2).pow(SQUARE) +
                        (rightVertexZ2 - leftVertexZ2).pow(SQUARE)

                val result3 = (rightVertexX3 - leftVertexX3).pow(SQUARE) +
                        (rightVertexY3 - leftVertexY3).pow(SQUARE) +
                        (rightVertexZ3 - leftVertexZ3).pow(SQUARE)

                val result4 = (rightVertexX4 - leftVertexX4).pow(SQUARE) +
                        (rightVertexY4 - leftVertexY4).pow(SQUARE) +
                        (rightVertexZ4 - leftVertexZ4).pow(SQUARE)

                val result5 = (rightVertexX5 - leftVertexX5).pow(SQUARE) +
                        (rightVertexY5 - leftVertexY5).pow(SQUARE) +
                        (rightVertexZ5 - leftVertexZ5).pow(SQUARE)

                val pupillaryDistance = sqrt(result) * 1000  // millimeter
                val pupillaryDistance2 = sqrt(result2) * 1000  // millimeter
                val pupillaryDistance3 = sqrt(result3) * 1000  // millimeter
                val pupillaryDistance4 = sqrt(result4) * 1000  // millimeter
                val pupillaryDistance5 = sqrt(result5) * 1000  // millimeter
                Timber.d("你的瞳距：$pupillaryDistance")

                /**
                 * todo 精度优化，有两种优化方案：
                 * 1、sdk优化
                 *    提供更加精确的也整点（其实单靠一组特征点不靠谱，最好的方案就是多组平均，但是sdk若能
                 *    像arkit那样提供更多的特征点更好）
                 * 2、选取大量特征点进行求平均值
                 *    官方给的"人脸模型图"特征点可选取的点位较少，且有些点位不容易观察，这点影响精度，最直观的就是
                 *    人脸移动时数据变动误差较大。
                 *
                 *  建议：目前测量时手机正对人脸，找不同的姿势选取数值取多组平均值。 （如自测三次取平均值）
                 * */

                // 数据太少 先用这两组测试 相对精确点

                val avl = (pupillaryDistance +
//                        pupillaryDistance2 +
//                        pupillaryDistance3 +
                        pupillaryDistance4
                        ) / 2.0
                val str = String.format("%.2f", avl)

                pdText.text = "your pd :$str mm"
            }

            // recycle
            val iterator: MutableIterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> =
                faceNodeMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry: Map.Entry<AugmentedFace, AugmentedFaceNode> = iterator.next()
                val face = entry.key
                if (face.trackingState == TrackingState.STOPPED) {
                    val faceNode = entry.value
                    faceNode.setParent(null)
                    iterator.remove()
                }
            }

        }
    }

    /**
     * 安卓7.0 opengl3.0 检测
     * */
    @SuppressLint("ObsoleteSdkInt")
    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Timber.d("SceneForm requires Android N or later")
            Toast.makeText(activity, "SceneForm requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Timber.d("SceneForm requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "SceneForm requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    private fun beforeSetContentView() {
        if (!checkIsSupportedDeviceOrFinish(this)) {
            finish()
        }
    }
}
```