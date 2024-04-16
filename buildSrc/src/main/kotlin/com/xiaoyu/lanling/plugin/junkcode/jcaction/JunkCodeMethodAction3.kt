package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import com.xiaoyu.lanling.plugin.junkcode.JunkCodeTransformer
import com.xiaoyu.lanling.plugin.junkcode.JunkCodeTransformer.Companion.generateName
import com.xiaoyu.lanling.plugin.junkcode.JunkCodeTransformer.Companion.random
import com.xiaoyu.lanling.plugin.utils.isBooleanDescriptor
import com.xiaoyu.lanling.plugin.utils.isIntDescriptor
import com.xiaoyu.lanling.plugin.utils.isStringDescriptor
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class JunkCodeMethodAction3 : JunkCodeMethod {

    override fun insertJunkCode(methodName: String, klass: ClassNode, notExecutedMethods: MutableMap<FunctionInfo, Int>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("Ljava/lang/String;", "Ljava/lang/String;"), "Ljava/lang/String;")
        notExecutedMethods[thisFun] = 0
        val methodVisitor = klass.visitMethod(Opcodes.ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null);
//        val annotationVisitor = methodVisitor.visitAnnotation("Landroidx/annotation/Keep;", false)
//        annotationVisitor.visitEnd()
        methodVisitor.visitCode()
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(Opcodes.LSTORE, 3)
        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName( arg1 : ")
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" , arg2 : ")
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" )")
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(Opcodes.POP)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        val label0 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IFNULL, label0)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label0)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
        methodVisitor.visitJumpInsn(Opcodes.IFNULL, label0)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        val label1 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IFNE, label1)
        methodVisitor.visitLabel(label0)
        methodVisitor.visitInsn(Opcodes.ACONST_NULL)
        methodVisitor.visitInsn(Opcodes.ARETURN)
        methodVisitor.visitLabel(label1)

        // 执行调用逻辑
        JunkCodeTransformer.callMethod(notExecutedMethods, thisFun, methodVisitor, klass)

        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 5)
        methodVisitor.visitInsn(Opcodes.ICONST_0)
        methodVisitor.visitVarInsn(Opcodes.ISTORE, 6)
        val label2 = Label()
        methodVisitor.visitLabel(label2)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 6)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 5)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        val label3 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, label3)
        methodVisitor.visitLdcInsn("println")
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("arg c : ")
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 5)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 6)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(Opcodes.POP)
        methodVisitor.visitIincInsn(6, 1)
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label2)
        methodVisitor.visitLabel(label3)
        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName executionTime : [")
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(Opcodes.LLOAD, 3)
        methodVisitor.visitInsn(Opcodes.LSUB)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" ] ms")
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(Opcodes.POP)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 5)
        methodVisitor.visitInsn(Opcodes.ARETURN)
        methodVisitor.visitMaxs(6, 7)
        methodVisitor.visitEnd()
    }
}