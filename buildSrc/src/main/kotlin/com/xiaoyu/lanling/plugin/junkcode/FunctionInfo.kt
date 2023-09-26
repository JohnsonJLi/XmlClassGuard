package com.xiaoyu.lanling.plugin.junkcode

import com.xiaoyu.lanling.plugin.utils.isBooleanDescriptor
import com.xiaoyu.lanling.plugin.utils.isDoubleDescriptor
import com.xiaoyu.lanling.plugin.utils.isIntDescriptor
import com.xiaoyu.lanling.plugin.utils.isStringDescriptor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

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

    fun defInParameter(methodVisitor: MethodVisitor, klass: ClassNode) {
        inParameter?.forEach { lastInParam ->
            if (lastInParam.isStringDescriptor) {
                if (JunkCodeTransformer.random.nextBoolean()) {
                    methodVisitor.visitLdcInsn(JunkCodeTransformer.generateName())
                } else {
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                    methodVisitor.visitFieldInsn(Opcodes.GETFIELD, klass.name, JunkCodeTransformer.FIELD_NAME_FROM, "Ljava/lang/String;");
                }
            } else if (lastInParam.isBooleanDescriptor) {
                methodVisitor.visitInsn(if (JunkCodeTransformer.random.nextBoolean()) Opcodes.ICONST_1 else Opcodes.ICONST_0)
            } else if (lastInParam.isIntDescriptor) {
                methodVisitor.visitIntInsn(Opcodes.SIPUSH, JunkCodeTransformer.random.nextInt())
            } else if (lastInParam.isDoubleDescriptor) {
                methodVisitor.visitLdcInsn(JunkCodeTransformer.random.nextDouble())
            }
        }
    }

}
