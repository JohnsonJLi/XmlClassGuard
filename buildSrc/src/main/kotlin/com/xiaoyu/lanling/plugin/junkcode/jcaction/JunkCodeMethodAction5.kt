package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import com.xiaoyu.lanling.plugin.junkcode.JunkCodeTransformer
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class JunkCodeMethodAction5 : JunkCodeMethod {

    override fun insertJunkCode(methodName: String, klass: ClassNode, notExecutedMethods: MutableMap<FunctionInfo, Int>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("D"), "D")
        notExecutedMethods[thisFun] = 0
        val methodVisitor = klass.visitMethod(Opcodes.ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null);

        methodVisitor.visitParameter("i", 0);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.DLOAD, 1);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
        methodVisitor.visitVarInsn(Opcodes.DSTORE, 3);
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        methodVisitor.visitLdcInsn("Square root of PI is: ");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        methodVisitor.visitVarInsn(Opcodes.DLOAD, 3);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(D)Ljava/lang/StringBuilder;", false);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        // 执行调用逻辑
        JunkCodeTransformer.callMethod(notExecutedMethods, thisFun, methodVisitor, klass)

        methodVisitor.visitVarInsn(Opcodes.DLOAD, 3);
        methodVisitor.visitInsn(Opcodes.DRETURN);
        methodVisitor.visitMaxs(4, 5);
        methodVisitor.visitEnd();


    }
}