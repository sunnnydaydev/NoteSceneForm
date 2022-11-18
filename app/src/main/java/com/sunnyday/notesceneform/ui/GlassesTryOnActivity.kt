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
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.sunnyday.notesceneform.R
import com.sunnyday.notesceneform.ui.fragments.FaceArFragment
import timber.log.Timber

class GlassesTryOnActivity : AppCompatActivity() {
    companion object {
        private const val MIN_OPENGL_VERSION = 3.0
    }

    private var foxFragment: FaceArFragment? = null
    private var faceRegionRenderable: Renderable? = null
    private val faceNodeMap = mutableMapOf<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish(this)) {
            finish()
            return
        }
        setContentView(R.layout.activity_glasses_try_on)
        foxFragment = supportFragmentManager.findFragmentById(R.id.vto) as FaceArFragment?

        // 1、构建可渲染对象
        ModelRenderable
            .builder()
            .setSource(this, R.raw.yellow_sunglasses)
            .build()
            .thenAccept {
                faceRegionRenderable = it
                it.isShadowCaster = false
                it.isShadowReceiver = false
            }


        // 3、获取arSceneView、Scene对象
        val arSceneView = foxFragment?.arSceneView
        val scene = arSceneView?.scene

        arSceneView?.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        // 4、场景实时帧更新监听
        scene?.addOnUpdateListener {
            if (faceRegionRenderable == null) return@addOnUpdateListener

            val faceList = arSceneView.session?.getAllTrackables(AugmentedFace::class.java)
            faceList?.forEach {

                if (!faceNodeMap.containsKey(it)) {
                    // 创建一个面部增强的节点（把面部信息传递过来）
                    val faceNode = AugmentedFaceNode(it)
                    // 进行面部增强渲染
                    faceNode.setParent(scene)
                    faceNode.renderable = faceRegionRenderable
                    faceNodeMap[it] = faceNode
                }
            }

            // 回收操作
            val iterator:MutableIterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> = faceNodeMap.entries.iterator()
            while (iterator.hasNext()){
                val entry: Map.Entry<AugmentedFace, AugmentedFaceNode> = iterator.next()
                val face = entry.key
                if (face.trackingState== TrackingState.STOPPED){
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
}
