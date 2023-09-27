package com.ljx.example.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jc.instrument.JCTools
import com.ljx.example.R

class MainActivity : AppCompatActivity() {

    var def_from: String = "def_from"

    init {
//        def_from = this.javaClass.simpleName
//        ttttst0(JCTools.getD())
//        ttttst33(JCTools.getD())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        findViewById<TextView>(R.id.tv_hello).setOnClickListener {
        startActivity(Intent(this, LoginActivity::class.java))
//        }
//        ststste7("from", 100)
//        ttttst2(def_from, JCTools.getI())

    }

    fun test(main: com.ljx.example.activity.MainActivity) {
        Log.d("LJX", main::class.java.name)
    }


    fun ttttst0(i: Double): Boolean {
        val y = Math.sqrt(i)
        println("Square root of PI is: $y")
        ttttst2(JCTools.getStr(), JCTools.getI())
        return true
    }

    fun ttttst2(str: String?, i: Int): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        Log.i("FULL_STACK", "ttttst0( arg1 : $str , arg2 : $i )")
        if (str == null || str.length == 0 || i <= 0 || str.length < i) {
            return false
        }
        val i2 = 202
        ttttst3(str + def_from, JCTools.getI() + i)
        Log.i("FULL_STACK", "ttttst0 executionTime : [" + (System.currentTimeMillis() - currentTimeMillis) + " ] ms")
        return true
    }

    fun ttttst3(str: String?, i: Int): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        Log.i("FULL_STACK", "ttttst0( arg1 : $str , arg2 : $i )")
        if (str == null || str.length == 0 || i <= 0 || str.length < i) {
            return false
        }
        val i2 = 202
        ttttst33(JCTools.getD())
        Log.i("FULL_STACK", "ttttst0 executionTime : [" + (System.currentTimeMillis() - currentTimeMillis) + " ] ms")
        return true
    }

    fun ttttst33(i: Double): Boolean {
        val y = Math.sqrt(i)
        println("Square root of PI is: $y")
        ttttst2(JCTools.getStr(), 123)
        return true
    }

}