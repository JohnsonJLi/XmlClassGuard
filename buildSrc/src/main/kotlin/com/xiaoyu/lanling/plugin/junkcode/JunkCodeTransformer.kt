package com.xiaoyu.lanling.plugin.junkcode

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.isAbstract
import com.google.auto.service.AutoService
import com.xiaoyu.lanling.plugin.utils.*
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import java.util.*


@AutoService(ClassTransformer::class)
class JunkCodeTransformer : ClassTransformer {

    companion object {
        var abc: CharArray? = null
        private const val GENERATE_MAIN_PACKAGE = "GENERATE_MAIN_PACKAGE"
        private const val GENERATE_MAIN_PACKAGE_METHOD_MULTIPLE = "GENERATE_MAIN_PACKAGE_METHOD_MULTIPLE"
        private const val GENERATE_MAIN_PACKAGE_DICTIONARY = "GENERATE_MAIN_PACKAGE_DICTIONARY"
        private const val DEFAULT_VALUE_DICTIONARY = "abcdefghijklmnopqrstuvwxyz"
    }


    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        val packageStr = context.getProperty(GENERATE_MAIN_PACKAGE, "")
        if (packageStr.isEmpty()) return klass
        val packageList = if (packageStr.contains(",")) packageStr.split(",").map { it.replace(".", "/") }
        else mutableListOf(packageStr.replace(".", "/"))
        if (!ClassUtils.checkClassName(klass.name)
            || klass.name.let {
                packageList.forEach { item ->
                    if (it.startsWith(item)) return@let false
                }
                true
            }
            || klass.name.contains("entity")
//            || klass.isFinal
            || klass.isAbstract
        ) return klass
        val notExecutedMethods = mutableListOf<FunctionInfo>()
        println("JunkCode Transforming ${klass.name}")
        val methodNames = ArrayList<String>()
        klass.methods.forEach { methodNames.add(it.name) }

        var methodCount = (if (klass.methods.isNotEmpty())
            (try {
                context.getProperty(GENERATE_MAIN_PACKAGE_METHOD_MULTIPLE, 2).toFloat()
            } catch (e: Exception) {
                2f
            } * klass.methods.size).toInt()
        else 10)
        if (methodCount < 5) methodCount = 5
        methodCount += random.nextInt(5) - 2

        if (abc == null || abc!!.isEmpty()) {
            abc = context.getProperty(GENERATE_MAIN_PACKAGE_DICTIONARY, DEFAULT_VALUE_DICTIONARY).toString().toCharArray()
            println("JunkCode Transforming abc: $abc")
        }

        for (i in 0..methodCount) {
            generateMethods(methodNames, generateName(i), klass, notExecutedMethods)
        }

        if (notExecutedMethods.isNotEmpty()) {
            var isInit = false
            klass.methods.forEach { method ->
                if (method.isInitMethod) {
                    val instructions = method.instructions
                    method.instructions?.iterator()?.forEach {
                        if ((it.opcode >= Opcodes.IRETURN && it.opcode <= Opcodes.RETURN) || it.opcode == Opcodes.ATHROW) {
                            while (notExecutedMethods.isNotEmpty()) {
                                val lastFun = notExecutedMethods[0]

                                instructions.insertBefore(it, VarInsnNode(ALOAD, 0))
                                lastFun.inParameter?.forEach { lastInParam ->
                                    if (lastInParam.isStringDescriptor) {
                                        instructions.insertBefore(it, LdcInsnNode(generateName()))
                                    } else if (lastInParam.isBooleanDescriptor) {
                                        instructions.insertBefore(it, InsnNode(if (random.nextBoolean()) ICONST_1 else ICONST_0))
                                    } else if (lastInParam.isIntDescriptor) {
                                        instructions.insertBefore(it, IntInsnNode(SIPUSH, random.nextInt(1000)))
                                    }
                                }

                                println("JunkCode <init> ${klass.name}  ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")

                                instructions.insertBefore(
                                    it,
                                    MethodInsnNode(INVOKEVIRTUAL, klass.name, lastFun.methodName, lastFun.getDescriptorStr(), false)
                                )
                                notExecutedMethods.remove(lastFun)
                            }
                        }
                    }
                    isInit = true
                }
            }
            if (!isInit) {
//                createInitFunction(classNode, this)
                println("JunkCode <init> does Not Exist")
            }
        }

        return klass
    }

    fun generateMethods(methodNames: ArrayList<String>, methodName: String, klass: ClassNode, notExecutedMethods: MutableList<FunctionInfo>) {
        if (!methodNames.contains(methodName)) {
            methodNames.add(methodName)
            val nextInt = random.nextInt(3)
            println("JunkCode generateMethods $nextInt add ${methodName}")
            when (nextInt) {
                0 -> insertJunkCode1(methodName, klass, notExecutedMethods)
                1 -> insertJunkCode2(methodName, klass, notExecutedMethods)
                else -> insertJunkCode3(methodName, klass, notExecutedMethods)
            }
        }
    }

    val random = Random()


    fun generateName(index: Int = random.nextInt(abc!!.size)): String {
        val sb = StringBuilder()
        for (i in 0..4) {
            sb.append(abc!![random.nextInt(abc!!.size)])
        }
        var temp = index
        while (temp >= 0) {
            sb.append(abc!![temp % abc!!.size])
            temp /= abc!!.size
            if (temp == 0) {
                temp = -1
            }
        }
        sb.append(index.toString())
        return sb.toString()
    }

    fun insertJunkCode1(methodName: String, klass: ClassNode, notExecutedMethods: MutableList<FunctionInfo>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("Ljava/lang/String;", "I"), "V")
        notExecutedMethods.add(thisFun)
        val methodVisitor = klass.visitMethod(ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null)
//        val annotationVisitor = methodVisitor.visitAnnotation("Landroidx/annotation/Keep;", false)
//        annotationVisitor.visitEnd()
        methodVisitor.visitCode()
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(LSTORE, 3)
        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName( arg1 : ")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" , arg2 : ")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(ILOAD, 2)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" )")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(POP)
        methodVisitor.visitVarInsn(ALOAD, 1)
        val label0 = Label()
        methodVisitor.visitJumpInsn(IFNULL, label0)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        val label1 = Label()
        methodVisitor.visitJumpInsn(IFNE, label1)
        methodVisitor.visitLabel(label0)
        methodVisitor.visitTypeInsn(NEW, "java/lang/NullPointerException")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitLdcInsn("Parameter NullPointerException")
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/NullPointerException", "<init>", "(Ljava/lang/String;)V", false)
        methodVisitor.visitInsn(ATHROW)
        methodVisitor.visitLabel(label1)
        methodVisitor.visitVarInsn(ILOAD, 2)
        val label2 = Label()
        methodVisitor.visitJumpInsn(IFGT, label2)
        methodVisitor.visitTypeInsn(NEW, "java/lang/RuntimeException")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitLdcInsn("Parameter Exceptions")
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false)
        methodVisitor.visitInsn(ATHROW)
        methodVisitor.visitLabel(label2)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        methodVisitor.visitVarInsn(ILOAD, 2)
        methodVisitor.visitInsn(IMUL)
        val label3 = Label()
        methodVisitor.visitJumpInsn(IFNE, label3)
        methodVisitor.visitTypeInsn(NEW, "java/lang/RuntimeException")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitLdcInsn("Parameter Exceptions")
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false)
        methodVisitor.visitInsn(ATHROW)
        methodVisitor.visitLabel(label3)

        // TODO: 执行调用逻辑
        if (notExecutedMethods.isNotEmpty()) {
            val lastFun = notExecutedMethods[0]
            if (lastFun != thisFun) {
                methodVisitor.visitVarInsn(ALOAD, 0)
                lastFun.inParameter?.forEach { lastInParam ->
                    if (lastInParam.isStringDescriptor) {
                        methodVisitor.visitLdcInsn(generateName())
                    } else if (lastInParam.isBooleanDescriptor) {
                        methodVisitor.visitInsn(if (random.nextBoolean()) ICONST_1 else ICONST_0)
                    } else if (lastInParam.isIntDescriptor) {
                        methodVisitor.visitIntInsn(SIPUSH, random.nextInt(1000))
                    }
                }

                println("JunkCode ${klass.name}  ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, klass.name, lastFun.methodName, lastFun.getDescriptorStr(), false)
//                methodVisitor.visitInsn(POP)
                notExecutedMethods.remove(lastFun)
            }
        }


        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName executionTime : ")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(LLOAD, 3)
        methodVisitor.visitInsn(LSUB)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" ms")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(POP)
        methodVisitor.visitInsn(RETURN)
        methodVisitor.visitMaxs(6, 5)
        methodVisitor.visitEnd()
    }

    fun insertJunkCode2(methodName: String, klass: ClassNode, notExecutedMethods: MutableList<FunctionInfo>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("Ljava/lang/String;", "I"), "Z")
        notExecutedMethods.add(thisFun)
        val methodVisitor = klass.visitMethod(ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null)
//        val annotationVisitor = methodVisitor.visitAnnotation("Landroidx/annotation/Keep;", false)
//        annotationVisitor.visitEnd()
        methodVisitor.visitCode()
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(LSTORE, 3)
        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName( arg1 : ")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" , arg2 : ")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(ILOAD, 2)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" )")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(POP)
        methodVisitor.visitVarInsn(ALOAD, 1)
        val label0 = Label()
        methodVisitor.visitJumpInsn(IFNULL, label0)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        val label1 = Label()
        methodVisitor.visitJumpInsn(IFNE, label1)
        methodVisitor.visitLabel(label0)
        methodVisitor.visitInsn(ICONST_0)
        methodVisitor.visitInsn(IRETURN)
        methodVisitor.visitLabel(label1)
        methodVisitor.visitVarInsn(ILOAD, 2)
        val label2 = Label()
        methodVisitor.visitJumpInsn(IFGT, label2)
        methodVisitor.visitInsn(ICONST_0)
        methodVisitor.visitInsn(IRETURN)
        methodVisitor.visitLabel(label2)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        methodVisitor.visitVarInsn(ILOAD, 2)
        val label3 = Label()
        methodVisitor.visitJumpInsn(IF_ICMPGE, label3)
        methodVisitor.visitInsn(ICONST_0)
        methodVisitor.visitInsn(IRETURN)
        methodVisitor.visitLabel(label3)

        // TODO: 执行调用逻辑
        if (notExecutedMethods.isNotEmpty()) {
            val lastFun = notExecutedMethods[0]
            if (lastFun != thisFun) {
                println("JunkCode notExecutedMethods ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                methodVisitor.visitVarInsn(ALOAD, 0)
                lastFun.inParameter?.forEach { lastInParam ->
                    if (lastInParam.isStringDescriptor) {
                        methodVisitor.visitLdcInsn(generateName())
                    } else if (lastInParam.isBooleanDescriptor) {
                        methodVisitor.visitInsn(if (random.nextBoolean()) ICONST_1 else ICONST_0)
                    } else if (lastInParam.isIntDescriptor) {
                        methodVisitor.visitIntInsn(SIPUSH, random.nextInt())
                    }
                }

                println("JunkCode ${klass.name}  ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, klass.name, lastFun.methodName, lastFun.getDescriptorStr(), false)
//                methodVisitor.visitInsn(POP)
                notExecutedMethods.remove(lastFun)
            }
        }


        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName executionTime : [")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(LLOAD, 3)
        methodVisitor.visitInsn(LSUB)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" ] ms")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(POP)
        methodVisitor.visitInsn(ICONST_1)
        methodVisitor.visitInsn(IRETURN)
        methodVisitor.visitMaxs(6, 5)
        methodVisitor.visitEnd()
    }

    fun insertJunkCode3(methodName: String, klass: ClassNode, notExecutedMethods: MutableList<FunctionInfo>) {
        val thisFun = FunctionInfo(methodName, mutableListOf("Ljava/lang/String;", "Ljava/lang/String;"), "Ljava/lang/String;")
        notExecutedMethods.add(thisFun)
        val methodVisitor = klass.visitMethod(ACC_PUBLIC, thisFun.methodName, thisFun.getDescriptorStr(), null, null);
//        val annotationVisitor = methodVisitor.visitAnnotation("Landroidx/annotation/Keep;", false)
//        annotationVisitor.visitEnd()
        methodVisitor.visitCode()
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(LSTORE, 3)
        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName( arg1 : ")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" , arg2 : ")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(ALOAD, 2)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" )")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(POP)
        methodVisitor.visitVarInsn(ALOAD, 1)
        val label0 = Label()
        methodVisitor.visitJumpInsn(IFNULL, label0)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        methodVisitor.visitJumpInsn(IFEQ, label0)
        methodVisitor.visitVarInsn(ALOAD, 2)
        methodVisitor.visitJumpInsn(IFNULL, label0)
        methodVisitor.visitVarInsn(ALOAD, 2)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false)
        val label1 = Label()
        methodVisitor.visitJumpInsn(IFNE, label1)
        methodVisitor.visitLabel(label0)
        methodVisitor.visitInsn(ACONST_NULL)
        methodVisitor.visitInsn(ARETURN)
        methodVisitor.visitLabel(label1)

        // TODO: 执行调用逻辑
        if (notExecutedMethods.isNotEmpty()) {
            val lastFun = notExecutedMethods[0]
            if (lastFun != thisFun) {
                println("JunkCode notExecutedMethods ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                methodVisitor.visitVarInsn(ALOAD, 0)
                lastFun.inParameter?.forEach { lastInParam ->
                    if (lastInParam.isStringDescriptor) {
                        methodVisitor.visitLdcInsn(generateName())
                    } else if (lastInParam.isBooleanDescriptor) {
                        methodVisitor.visitInsn(if (random.nextBoolean()) ICONST_1 else ICONST_0)
                    } else if (lastInParam.isIntDescriptor) {
                        methodVisitor.visitIntInsn(SIPUSH, random.nextInt())
                    }
                }

                println("JunkCode ${klass.name}  ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, klass.name, lastFun.methodName, lastFun.getDescriptorStr(), false)
//                methodVisitor.visitInsn(POP)
                notExecutedMethods.remove(lastFun)
            }
        }

        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitVarInsn(ALOAD, 2)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitVarInsn(ASTORE, 5)
        methodVisitor.visitLdcInsn("FULL_STACK")
        methodVisitor.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        methodVisitor.visitLdcInsn("$methodName executionTime : [")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
        methodVisitor.visitVarInsn(LLOAD, 3)
        methodVisitor.visitInsn(LSUB)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitLdcInsn(" ] ms")
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        methodVisitor.visitInsn(POP)
        methodVisitor.visitVarInsn(ALOAD, 5)
        methodVisitor.visitInsn(ARETURN)
        methodVisitor.visitMaxs(6, 6)
        methodVisitor.visitEnd()
    }
}