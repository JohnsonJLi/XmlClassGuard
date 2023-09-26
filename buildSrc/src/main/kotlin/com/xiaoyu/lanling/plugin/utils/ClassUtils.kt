package com.xiaoyu.lanling.plugin.utils

import java.io.File
import java.io.FileOutputStream

object ClassUtils {
    fun path2Classname(entryName: String): String {
        // 感谢大佬 dingshaoran
        //ClassUtils.path2Classname(className); File.separator donot match jar entryName on windows
        return entryName.replace(".class", "")
            .replace('\\', '.')
            .replace('/', '.')
    }

    fun checkClassName(className: String): Boolean {
        if (className.contains("R\$")) {
            return false
        }
        if (className.endsWith("R.class")) {
            return false
        }
//        if (className.startsWith("com/chad/library")) {
//            return false
//        }
        return (!className.contains("R\\$") && !className.endsWith("R")
                && !className.endsWith("BuildConfig"))
    }

    fun saveFile(mTempDir: File?, modifiedClassBytes: ByteArray?): File? {
        val modified: File? = mTempDir
        modifiedClassBytes?.apply {
            if (mTempDir!!.exists()) {
                mTempDir.delete()
            }
            mTempDir.createNewFile()
            val stream = FileOutputStream(mTempDir)
            stream.use {
                stream.write(modifiedClassBytes)
            }
        }
        return modified
    }
}