package com.sunnyday.notesceneform.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.sunnyday.notesceneform.R

class ArRulerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_ruler)

    }

    /**
     * 安卓7.0 opengl3.0 检测
     * */
    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e("ArRulerActivity", "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < 3.0) {
            Log.e("ArRulerActivity", "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }
}