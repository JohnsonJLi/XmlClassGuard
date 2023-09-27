package com.jc.instrument

import android.app.Application
import android.content.res.AssetManager
import ggg.kkk.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

object JCTools {
    var app: Application? = null

    fun isOpen(): Boolean {
        return BuildConfig.DEBUG ?: false
    }

    private val random = Random()

    private val jlcDispositionData: JlcDispositionData? by lazy {
        try {
            app ?: return@lazy null
            val assetManager: AssetManager = app!!.assets
            val inputStream = assetManager.open("disposition.json")
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            val json = stringBuilder.toString()
            val jsonObject = JSONObject(json)

            JlcDispositionData(
                jsonObject.optJSONArray("s").let { array ->
                    val list = mutableListOf<String>()
                    for (i in 0 until array.length()) {
                        list.add(array.getString(i))
                    }
                    return@let list
                },
                jsonObject.optJSONArray("i").let { array ->
                    val list = mutableListOf<Int>()
                    for (i in 0 until array.length()) {
                        list.add(array.getInt(i))
                    }
                    return@let list
                },
                jsonObject.optJSONArray("d").let { array ->
                    val list = mutableListOf<Double>()
                    for (i in 0 until array.length()) {
                        list.add(array.getDouble(i))
                    }
                    return@let list
                },
                jsonObject.optJSONArray("z").let { array ->
                    val list = mutableListOf<Boolean>()
                    for (i in 0 until array.length()) {
                        list.add(array.getBoolean(i))
                    }
                    return@let list
                },
                jsonObject.optJSONArray("f").let { array ->
                    val list = mutableListOf<Float>()
                    for (i in 0 until array.length()) {
                        list.add(array.getString(i).toFloat())
                    }
                    return@let list
                },
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getStr() = try {
        jlcDispositionData!!.s[random.nextInt(jlcDispositionData!!.s.size)]
    } catch (e: Exception) {
        ""
    }

    fun getD() = try {
        jlcDispositionData!!.d[random.nextInt(jlcDispositionData!!.d.size)]
    } catch (e: Exception) {
        random.nextInt(100).toDouble()
    }

    fun getZ() = try {
        jlcDispositionData!!.z[random.nextInt(jlcDispositionData!!.z.size)]
    } catch (e: Exception) {
        random.nextBoolean()
    }

    fun getI() = try {
        jlcDispositionData!!.i[random.nextInt(jlcDispositionData!!.i.size)]
    } catch (e: Exception) {
        random.nextInt(100)
    }
}

