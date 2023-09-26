package com.xiaoyu.lanling.plugin.junkcode

import jdk.internal.org.objectweb.asm.MethodVisitor

data class FunctionInfo(val methodName: String, val inParameter: List<String>? = null, val outParameter: String? = null) {
    fun getDescriptorStr(): String {
        val sb = java.lang.StringBuilder("(")
        inParameter?.forEach {
            sb.append(it)
        }
        sb.append(")")
        if (outParameter.isNullOrEmpty().not()) sb.append(outParameter)
        else sb.append("V")
        return sb.toString()
    }

//    val usedCodeMap = hashMapOf<Int, Int>()
//
//    fun visitVarInsn(methodVisitor: MethodVisitor, paramDesc: String?, nonStatic: Boolean = true): Boolean {
//        if (paramDesc == null || inParameter.isNullOrEmpty()) return false
//        val codes = mutableSetOf<Int>()
//        inParameter.forEachIndexed { index, s ->
//            if (s == paramDesc) {
//                codes.add(index)
//            }
//        }
//        if (codes.isNotEmpty()) {
//            codes.forEach {
//                val count = usedCodeMap[it] ?: 0
//                usedCodeMap[it] = count + 1
//                if (count >= 2) {
//                    methodVisitor.visitLdcInsn("str")
//                }else{
//
//                }
//            }
//        }
//        return true
//    }

}
