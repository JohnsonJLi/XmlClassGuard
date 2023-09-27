package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import com.xiaoyu.lanling.plugin.junkcode.JunkCodeTransformer
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class JunkCodeMethodAction1 : JunkCodeMethod {

    override fun insertJunkCode(methodName: String, klass: ClassNode, notExecutedMethods: MutableMap<FunctionInfo, Int>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("Ljava/lang/String;", "I"), "V")
        notExecutedMethods[thisFun] = 0
        val methodVisitor = klass.visitMethod(Opcodes.ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null)
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
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitLdcInsn(" , arg2 : ")
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 2)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" )")
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(Opcodes.POP)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        val label0 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IFNULL, label0)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        val label1 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IFNE, label1)
        methodVisitor.visitLabel(label0)
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/NullPointerException")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitLdcInsn("Parameter NullPointerException")
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V", false)
        methodVisitor.visitInsn(Opcodes.ATHROW)
        methodVisitor.visitLabel(label1)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 2)
        val label2 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IFGT, label2)
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitLdcInsn("Parameter Exceptions 1")
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false)
        methodVisitor.visitInsn(Opcodes.ATHROW)
        methodVisitor.visitLabel(label2)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 2)
        methodVisitor.visitInsn(Opcodes.IMUL)
        val label3 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IFNE, label3)
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitLdcInsn("Parameter Exceptions 2")
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false)
        methodVisitor.visitInsn(Opcodes.ATHROW)
        methodVisitor.visitLabel(label3)

        // 执行调用逻辑
        JunkCodeTransformer.callMethod(notExecutedMethods, thisFun, methodVisitor, klass)

        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName executionTime : ")
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(Opcodes.LLOAD, 3)
        methodVisitor.visitInsn(Opcodes.LSUB)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" ms")
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/StringBuilder",
            "append",
            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
            false
        )
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(Opcodes.POP)
        methodVisitor.visitInsn(Opcodes.RETURN)
        methodVisitor.visitMaxs(6, 5)
        methodVisitor.visitEnd()
    }
}