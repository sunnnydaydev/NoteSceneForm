package com.sunnyday.notesceneform.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ux.ArFragment
import java.util.*

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