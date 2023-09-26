package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import jdk.internal.org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class JunkCodeMethodAction5 : JunkCodeMethod {

    override fun insertJunkCode(methodName: String, klass: ClassNode, notExecutedMethods: MutableList<FunctionInfo>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("D"), "D")
        notExecutedMethods.add(thisFun)
        val methodVisitor = klass.visitMethod(Opcodes.ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null);

        methodVisitor.visitParameter("i", 0);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(DLOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sqrt", "(D)D", false);
        methodVisitor.visitVarInsn(DSTORE, 3);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        methodVisitor.visitLdcInsn("Square root of PI is: ");
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        methodVisitor.visitVarInsn(DLOAD, 3);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(D)Ljava/lang/StringBuilder;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        // 执行调用逻辑
        if (notExecutedMethods.isNotEmpty()) {
            val lastFun = notExecutedMethods[0]
            if (lastFun != thisFun) {
                println("JunkCode notExecutedMethods ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                lastFun.defInParameter(methodVisitor, klass)

                println("JunkCode ${klass.name}  ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, klass.name, lastFun.methodName, lastFun.getDescriptorStr(), false)
                notExecutedMethods.remove(lastFun)
            }
        }

        methodVisitor.visitVarInsn(DLOAD, 3);
        methodVisitor.visitInsn(DRETURN);
        methodVisitor.visitMaxs(4, 5);
        methodVisitor.visitEnd();


    }
}