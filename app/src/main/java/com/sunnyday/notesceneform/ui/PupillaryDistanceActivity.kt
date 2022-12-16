package com.sunnyday.notesceneform.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.sunnyday.notesceneform.R
import com.sunnyday.notesceneform.ui.fragments.FaceArFragment
import kotlinx.android.synthetic.main.activity_pupillary_distance.*
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.Format
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * use google arCore to get pupillary distance.
 * */
class PupillaryDistanceActivity : AppCompatActivity() {


    companion object {
        private const val MIN_OPENGL_VERSION = 3.0
        private const val SQUARE = 2.0
    }

    private val faceNodeMap = mutableMapOf<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beforeSetContentView()
        setContentView(R.layout.activity_pupillary_distance)

        var renderable: Renderable? = null
        ViewRenderable
            .builder()
            .setView(this, R.layout.layout_pd_model)
            .build().thenAccept {
                renderable = it
            }


        val arFragment = supportFragmentManager.findFragmentById(R.id.pupil) as FaceArFragment?
        val arSceneView = arFragment?.arSceneView

        arSceneView?.scene?.addOnUpdateListener {
            val faceList = arSceneView.session?.getAllTrackables(AugmentedFace::class.java)
            faceList?.forEach {


                val cameraPose = arSceneView.arFrame?.camera?.pose
                // 摄像机-> 鼻尖距离
                val norsePose = it.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
                val dx = cameraPose?.tx()?.minus(norsePose.tx())
                val dy = cameraPose?.ty()?.minus(norsePose.ty())
                val dz = cameraPose?.tz()?.minus(norsePose.tz())
                val dis = sqrt(
                    dx!!.toDouble().pow(SQUARE) +
                            dy!!.toDouble().pow(SQUARE) +
                            dz!!.toDouble().pow(SQUARE)
                ) * 100

                // 摄像机-> 左前额
                val foreHeadLeftPose = it.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT)
                val dxForeHeadLeft = cameraPose.tx().minus(foreHeadLeftPose.tx())
                val dyForeHeadLeft  = cameraPose.ty().minus(foreHeadLeftPose.ty())
                val dzForeHeadLeft  = cameraPose.tz().minus(foreHeadLeftPose.tz())
                val disForeHeadLeft  = sqrt(
                    dxForeHeadLeft.toDouble().pow(SQUARE) +
                            dyForeHeadLeft.toDouble().pow(SQUARE) +
                            dzForeHeadLeft.toDouble().pow(SQUARE)
                ) * 100
                // 右前额

                val foreHeadRightPose = it.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT)
                val dxForeHeadRight = cameraPose.tx().minus(foreHeadRightPose.tx())
                val dyForeHeadRight = cameraPose.ty().minus(foreHeadRightPose.ty())
                val dzForeHeadRight = cameraPose.tz().minus(foreHeadRightPose.tz())
                val disForeHeadRight  = sqrt(
                    dxForeHeadRight.toDouble().pow(SQUARE) +
                            dyForeHeadRight.toDouble().pow(SQUARE) +
                            dzForeHeadRight.toDouble().pow(SQUARE)
                ) * 100

                Timber.d("toNorse:$dis cm toForeHeadLeft$disForeHeadLeft cm toForeHeadRight$disForeHeadRight cm")

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
                 *   发现：使用第一组特征点相对来说精确点。
                 *
                 *
                 * */


                //眼周特征点上三组：

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

                val leftVertexX3 = it.meshVertices[3 * 158 - 3].toDouble()
                val leftVertexY3 = it.meshVertices[3 * 158 - 2].toDouble()
                val leftVertexZ3 = it.meshVertices[3 * 158 - 1].toDouble()
                val rightVertexX3 = it.meshVertices[3 * 387 - 3].toDouble()
                val rightVertexY3 = it.meshVertices[3 * 387 - 2].toDouble()
                val rightVertexZ3 = it.meshVertices[3 * 387 - 1].toDouble()

                //眼周特征点下三组：
                val leftVertexX4 = it.meshVertices[3 * 144 - 3].toDouble()
                val leftVertexY4 = it.meshVertices[3 * 144 - 2].toDouble()
                val leftVertexZ4 = it.meshVertices[3 * 144 - 1].toDouble()
                val rightVertexX4 = it.meshVertices[3 * 380 - 3].toDouble()
                val rightVertexY4 = it.meshVertices[3 * 380 - 2].toDouble()
                val rightVertexZ4 = it.meshVertices[3 * 380 - 1].toDouble()

                val leftVertexX5 = it.meshVertices[3 * 145 - 3].toDouble()
                val leftVertexY5 = it.meshVertices[3 * 145 - 2].toDouble()
                val leftVertexZ5 = it.meshVertices[3 * 145 - 1].toDouble()
                val rightVertexX5 = it.meshVertices[3 * 374 - 3].toDouble()
                val rightVertexY5 = it.meshVertices[3 * 374 - 2].toDouble()
                val rightVertexZ5 = it.meshVertices[3 * 374 - 1].toDouble()


                val leftVertexX6 = it.meshVertices[3 * 153 - 3].toDouble()
                val leftVertexY6 = it.meshVertices[3 * 153 - 2].toDouble()
                val leftVertexZ6 = it.meshVertices[3 * 153 - 1].toDouble()
                val rightVertexX6 = it.meshVertices[3 * 373 - 3].toDouble()
                val rightVertexY6 = it.meshVertices[3 * 373 - 2].toDouble()
                val rightVertexZ6 = it.meshVertices[3 * 373 - 1].toDouble()

                // 特征点眼周混合数据（不一定按照点的对称关系选取）

                val leftVertexXOther = it.meshVertices[3 * 160 - 3].toDouble()
                val leftVertexYOther = it.meshVertices[3 * 160 - 2].toDouble()
                val leftVertexZOther= it.meshVertices[3 * 160 - 1].toDouble()
                val rightVertexXOther = it.meshVertices[3 * 386 - 3].toDouble()
                val rightVertexYOther= it.meshVertices[3 * 386 - 2].toDouble()
                val rightVertexZOther= it.meshVertices[3 * 386 - 1].toDouble()


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

                val result6 = (rightVertexX6 - leftVertexX6).pow(SQUARE) +
                        (rightVertexY6 - leftVertexY6).pow(SQUARE) +
                        (rightVertexZ6 - leftVertexZ6).pow(SQUARE)

                val resultOther = (rightVertexXOther - leftVertexXOther).pow(SQUARE) +
                        (rightVertexYOther - leftVertexYOther).pow(SQUARE) +
                        (rightVertexZOther - leftVertexZOther).pow(SQUARE)

                val pupillaryDistance =  sqrt(result) * 1000   // millimeter
                val pupillaryDistance2 = sqrt(result2) * 1000  // millimeter
                val pupillaryDistance3 = sqrt(result3) * 1000  // millimeter
                val pupillaryDistance4 = sqrt(result4) * 1000  // millimeter
                val pupillaryDistance5 = sqrt(result5) * 1000  // millimeter
                val pupillaryDistance6 = sqrt(result6) * 1000  // millimeter
                val pupillaryDistanceOther = sqrt(resultOther) * 1000  // millimeter
              //  Timber.d("你的瞳距：$pupillaryDistance3")


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

                if (dis<29.5){
                    pdText.text =  "离远些"// 离近些
                }else if (dis>30.5){
                    pdText.text =  "离近些"// 离远些
                }else{
                    val pd = String.format("%.2f", pupillaryDistanceOther)
                    val norse =  String.format("%.2f", dis)
                    val leftHead = String.format("%.2f", disForeHeadLeft)
                    val rightHead = String.format("%.2f", disForeHeadRight)
                    pdText.text = "pd:$pd \n norse:$norse \n leftHead：$leftHead \n rightHead:$rightHead"
                }

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