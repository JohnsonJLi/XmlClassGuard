package com.ljx.example

import android.app.Application
import com.jc.instrument.JCTools

/**
 * User: ljx
 * Date: 2022/3/22
 * Time: 17:15
 */
class AppHolder : Application() {

    override fun onCreate() {
        super.onCreate()

        JCTools.app = this
    }
}