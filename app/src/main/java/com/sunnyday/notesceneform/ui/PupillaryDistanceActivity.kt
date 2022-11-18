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
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.sunnyday.notesceneform.R
import com.sunnyday.notesceneform.ui.fragments.FaceArFragment
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

        val arFragment = supportFragmentManager.findFragmentById(R.id.pupil) as FaceArFragment?
        val arSceneView = arFragment?.arSceneView
        val scene = arSceneView?.scene
        arSceneView?.scene?.addOnUpdateListener {
            val faceList = arSceneView.session?.getAllTrackables(AugmentedFace::class.java)
            faceList?.forEach {

                if (!faceNodeMap.containsKey(it)) {
                    val faceNode = AugmentedFaceNode(it)
                    faceNode.setParent(scene)
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
                 *   找下计算公式
                 *
                 * */
                //
                Timber.d("所有数据：${it.meshVertices.position()}")

                //取瞳孔数据

                val leftVertexX = it.meshVertices[3*160-3]
                val leftVertexY = it.meshVertices[3*160-2]
                val leftVertexZ = it.meshVertices[3*160-1]

                val rightVertexX = it.meshVertices[3*385-3]
                val rightVertexY = it.meshVertices[3*385-2]
                val rightVertexZ = it.meshVertices[3*385-1]

                Timber.d("leftVertex:($leftVertexX,$leftVertexY,$leftVertexZ)")
                Timber.d("rightVertex:($rightVertexX,$rightVertexY,$rightVertexZ)")
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