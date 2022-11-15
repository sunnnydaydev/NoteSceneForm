package com.sunnyday.notesceneform.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.sunnyday.notesceneform.R
import java.util.function.Function


class HelloSceneFormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_scene_form)

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
    }
}