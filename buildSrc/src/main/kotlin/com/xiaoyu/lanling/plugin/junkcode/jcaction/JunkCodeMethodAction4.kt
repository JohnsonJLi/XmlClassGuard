package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import com.xiaoyu.lanling.plugin.junkcode.JunkCodeTransformer
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class JunkCodeMethodAction4 : JunkCodeMethod {

    override fun insertJunkCode(methodName: String, klass: ClassNode, notExecutedMethods: MutableMap<FunctionInfo, Int>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("I"), "V")
        notExecutedMethods[thisFun] = 0
        val methodVisitor = klass.visitMethod(Opcodes.ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null);

        methodVisitor.visitParameter("i", 0)
        methodVisitor.visitCode()
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 1)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false)
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 2)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
        methodVisitor.visitVarInsn(Opcodes.ASTORE, 3)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 3)
        methodVisitor.visitInsn(Opcodes.ARRAYLENGTH)
        methodVisitor.visitVarInsn(Opcodes.ISTORE, 4)
        methodVisitor.visitInsn(Opcodes.ICONST_0)
        methodVisitor.visitVarInsn(Opcodes.ISTORE, 5)
        val label0 = Label()
        methodVisitor.visitLabel(label0)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 5)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 4)
        val label1 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, label1)
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 3)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 5)
        methodVisitor.visitInsn(Opcodes.CALOAD)
        methodVisitor.visitVarInsn(Opcodes.ISTORE, 6)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 6)
        methodVisitor.visitInsn(Opcodes.ICONST_2)
        methodVisitor.visitInsn(Opcodes.IREM)
        val label2 = Label()
        methodVisitor.visitJumpInsn(Opcodes.IFNE, label2)
        methodVisitor.visitLdcInsn("value print")
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 6)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" is even number.")
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
        val label3 = Label()
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label3)
        methodVisitor.visitLabel(label2)
        methodVisitor.visitLdcInsn("value print")
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(Opcodes.DUP)
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 6)
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" is odd number.")
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
        methodVisitor.visitLabel(label3)
        methodVisitor.visitIincInsn(5, 1)
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label0)
        methodVisitor.visitLabel(label1)

        // 执行调用逻辑
        JunkCodeTransformer.callMethod(notExecutedMethods, thisFun, methodVisitor, klass)

        methodVisitor.visitInsn(Opcodes.RETURN)
        methodVisitor.visitMaxs(3, 7)
        methodVisitor.visitEnd()


    }
}