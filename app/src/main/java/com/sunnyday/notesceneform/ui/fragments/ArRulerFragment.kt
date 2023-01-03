package com.sunnyday.notesceneform.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ar.sceneform.ux.ArFragment


/**
 * Create by SunnyDay /01/03 17:54:12
 */
class ArRulerFragment : ArFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val frameLayout = super.onCreateView(inflater, container, savedInstanceState) as? FrameLayout
        //隐藏指示物
        planeDiscoveryController.hide()
        //指示物布局置空
        planeDiscoveryController.setInstructionView(null)
        return  frameLayout
    }
}