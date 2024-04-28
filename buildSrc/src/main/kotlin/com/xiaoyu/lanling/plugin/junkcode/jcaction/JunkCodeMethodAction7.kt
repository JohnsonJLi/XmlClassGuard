package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import com.xiaoyu.lanling.plugin.junkcode.JunkCodeTransformer
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.util.List
import kotlin.random.Random


class JunkCodeMethodAction7 : JunkCodeMethod {

    private val PRIMITIVE_TYPES = listOf(
        "I",
        //"J", "F",
        "D",
        "Ljava/lang/String;"
        //"Ljava/lang/Object;"
    )
    private val BUILTIN_METHODS = listOf(
        "java/lang/Math/abs",
        "java/lang/Math/pow",
        "java/lang/Math/random",
        "java/lang/System/currentTimeMillis",
        "java/lang/System/nanoTime",
        "java/lang/Thread/currentThread/getId"
    )

    override fun insertJunkCode(funcName: String, classNode: ClassNode, notExecutedMethods: MutableMap<FunctionInfo, Int>) {

        // 生成随机入参列表
        val randomParamTypes: MutableList<String> = ArrayList()
        val numParams: Int = Random.nextInt(2, 6) // 随机生成2到5个参数
        for (i in 0 until numParams) {
            randomParamTypes.add(PRIMITIVE_TYPES.random())
        }
        val randomReturnType = PRIMITIVE_TYPES[Random.nextInt(PRIMITIVE_TYPES.size)]
        val thisFun = FunctionInfo(funcName, randomParamTypes, randomReturnType)
        notExecutedMethods[thisFun] = 0
        val methodVisitor: MethodVisitor = classNode.visitMethod(
            Opcodes.ACC_PUBLIC,
            thisFun.methodName,
            thisFun.getDescriptorStr(),
            null,
            null
        )

        // 添加参数
        var paramIndex = 1
        for (paramType in randomParamTypes) {
            methodVisitor.visitParameter("param" + paramIndex++, 0)
        }
        methodVisitor.visitCode()

        // 随机生成局部变量并初始化
        val localVarCount: Int = Random.nextInt(2, 6) // 随机生成2到5个局部变量
        for (i in 0 until localVarCount) {
            val localVarType = PRIMITIVE_TYPES[Random.nextInt(PRIMITIVE_TYPES.size)]
            val localVarIndex = Random.nextInt(Short.MAX_VALUE.toInt())
            when (localVarType) {
                "I" -> methodVisitor.visitInsn(Opcodes.ICONST_0 + Random.nextInt(5)) // 初始化为常量0到4
                "J" -> methodVisitor.visitInsn(Opcodes.LCONST_0 + if (Random.nextBoolean()) 1 else 0) // 初始化为常量0或1
                "F" -> methodVisitor.visitInsn(Opcodes.FCONST_0 + Random.nextInt(2)) // 初始化为常量0.0, 1.0, or 2.0
                "D" -> methodVisitor.visitInsn(Opcodes.DCONST_0 + if (Random.nextBoolean()) 1 else 0) // 初始化为常量0.0 or 1.0
                "Ljava/lang/Object;" -> methodVisitor.visitInsn(Opcodes.ACONST_NULL) // 初始化为null
//                "Ljava/lang/String;" -> thisFun.getDefString(methodVisitor, classNode)
                "Ljava/lang/String;" -> methodVisitor.visitInsn(Opcodes.ACONST_NULL) // 初始化为null
            }
            methodVisitor.visitVarInsn(getVarInsnByType(localVarType), localVarIndex)
        }

        // 执行调用逻辑
//        JunkCodeTransformer.callMethod(notExecutedMethods, thisFun, methodVisitor, classNode)

        // 随机生成逻辑
        val logicCount: Int = Random.nextInt(2, 6) // 随机生成2到5条逻辑语句
        for (i in 0 until logicCount) {
            val builtinMethod = BUILTIN_METHODS[Random.nextInt(BUILTIN_METHODS.size)]

            // 分割方法名和参数类型
            val parts = builtinMethod.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val className = parts[0]
            val methodName = parts[1]
            val returnType = parts[2].split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

            // 调用内置方法
            methodVisitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                className.replace('/', '.'),
                methodName,
                "()$returnType",
                false
            )
        }
        when (randomReturnType) {
            "I" -> methodVisitor.visitInsn(Opcodes.IRETURN)
            "J" -> methodVisitor.visitInsn(Opcodes.LRETURN)
            "F" -> methodVisitor.visitInsn(Opcodes.FRETURN)
            "D" -> methodVisitor.visitInsn(Opcodes.DRETURN)
            "Ljava/lang/Object;" -> methodVisitor.visitInsn(Opcodes.ARETURN)
            "Ljava/lang/String;" -> methodVisitor.visitInsn(Opcodes.ARETURN)
        }
        methodVisitor.visitMaxs(0, 0)
        methodVisitor.visitEnd()

    }

    private fun getVarInsnByType(type: String): Int {
        return when (type) {
            "I" -> Opcodes.ISTORE
            "J" -> Opcodes.LSTORE
            "F" -> Opcodes.FSTORE
            "D" -> Opcodes.DSTORE
            "Ljava/lang/Object;" -> Opcodes.ASTORE
            "Ljava/lang/String;" -> Opcodes.ASTORE
            else -> throw IllegalArgumentException("Unsupported local variable type: $type")
        }
    }

}

