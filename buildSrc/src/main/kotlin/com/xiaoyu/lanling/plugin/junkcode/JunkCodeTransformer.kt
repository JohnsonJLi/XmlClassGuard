package com.xiaoyu.lanling.plugin.junkcode

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.isAbstract
import com.google.auto.service.AutoService
import com.xiaoyu.lanling.plugin.junkcode.jcaction.*
import com.xiaoyu.lanling.plugin.utils.*
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@AutoService(ClassTransformer::class)
class JunkCodeTransformer : ClassTransformer {

    companion object {
        var abc: CharArray? = null
        private const val GENERATE_MAIN_PACKAGE = "GENERATE_MAIN_PACKAGE"
        const val FIELD_NAME_FROM = "mCpJuCoFrom"
        private const val GENERATE_MAIN_PACKAGE_METHOD_MULTIPLE = "GENERATE_MAIN_PACKAGE_METHOD_MULTIPLE"
        private const val GENERATE_MAIN_PACKAGE_DICTIONARY = "GENERATE_MAIN_PACKAGE_DICTIONARY"
        private const val DEFAULT_VALUE_DICTIONARY = "abcdefghijklmnopqrstuvwxyz"

        private val junkCodeMethods = arrayOf(
            JunkCodeMethodAction1(),
            JunkCodeMethodAction2(),
            JunkCodeMethodAction3(),
            JunkCodeMethodAction4(),
            JunkCodeMethodAction5(),
        )

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

        fun callMethod(
            notExecutedMethods: MutableMap<FunctionInfo, Int>,
            thisFun: FunctionInfo,
            methodVisitor: MethodVisitor,
            klass: ClassNode
        ) {
            if (notExecutedMethods.isNotEmpty()) {
                val iterator: Iterator<Map.Entry<FunctionInfo, Int>> = notExecutedMethods.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    val lastFun = next.key
                    if (lastFun != thisFun) {
                        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                        lastFun.defInParameter(methodVisitor, klass)

                        println("JunkCode ${klass.name}  ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, klass.name, lastFun.methodName, lastFun.getDescriptorStr(), false)
                        if (next.value + 1 >= 2) notExecutedMethods.remove(lastFun)
                        else notExecutedMethods[lastFun] = next.value + 1
                    }
                }
            }
        }

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
        val notExecutedMethods = ConcurrentHashMap<FunctionInfo, Int>()
        println("JunkCode Transforming ${klass.name}")

        val mJuCoFromFieldNode = FieldNode(ACC_PRIVATE, FIELD_NAME_FROM, "Ljava/lang/String;", null, null)
        klass.fields.add(mJuCoFromFieldNode)

        initClassField(klass)


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

        insertInit(notExecutedMethods, klass)

        return klass
    }

    private fun insertInit(
        notExecutedMethods: MutableMap<FunctionInfo, Int>,
        klass: ClassNode
    ) {
        if (notExecutedMethods.isNotEmpty()) {
            var isInit = false
            klass.methods.forEach { method ->
                if (method.isInitMethod) {
                    val instructions = method.instructions
                    method.instructions?.iterator()?.forEach {
                        if ((it.opcode >= IRETURN && it.opcode <= RETURN) || it.opcode == ATHROW) {

                            val startTry = LabelNode(Label())
                            val endTry = LabelNode(Label())
                            val startCatch = LabelNode(Label())

                            method.tryCatchBlocks.add(TryCatchBlockNode(startTry, endTry, startCatch, "java/lang/Exception"))

                            instructions.insertBefore(it, startTry)

                            val iterator: Iterator<Map.Entry<FunctionInfo, Int>> = notExecutedMethods.iterator()
                            while (iterator.hasNext()) {
                                val next = iterator.next()
                                if (next.value == 0) {
                                    val lastFun = next.key

                                    instructions.insertBefore(it, VarInsnNode(ALOAD, 0))
                                    lastFun.inParameter?.forEach { lastInParam ->
                                        if (lastInParam.isStringDescriptor) {
                                            if (random.nextBoolean()) {
                                                instructions.insertBefore(it, LdcInsnNode(generateName()))
                                            } else {
                                                instructions.insertBefore(it, VarInsnNode(ALOAD, 0))
                                                instructions.insertBefore(
                                                    it,
                                                    FieldInsnNode(GETFIELD, klass.name, FIELD_NAME_FROM, "Ljava/lang/String;")
                                                )
                                            }
                                        } else if (lastInParam.isBooleanDescriptor) {
                                            instructions.insertBefore(it, InsnNode(if (random.nextBoolean()) ICONST_1 else ICONST_0))
                                        } else if (lastInParam.isIntDescriptor) {
                                            instructions.insertBefore(it, IntInsnNode(SIPUSH, random.nextInt(1000)))
                                        } else if (lastInParam.isDoubleDescriptor) {
                                            instructions.insertBefore(it, LdcInsnNode(random.nextDouble()))
                                        }
                                    }

                                    println("JunkCode <init> ${klass.name}  ${lastFun.methodName}  ${lastFun.getDescriptorStr()}")

                                    instructions.insertBefore(
                                        it,
                                        MethodInsnNode(INVOKEVIRTUAL, klass.name, lastFun.methodName, lastFun.getDescriptorStr(), false)
                                    )

                                    lastFun.outParameter?.let { op ->
                                        if (op.isStringDescriptor
                                            || op.isIntDescriptor
                                            || op.isBooleanDescriptor
                                        ) {
                                            instructions.insertBefore(it, InsnNode(POP));
                                        } else if (op.isDoubleDescriptor) {
                                            instructions.insertBefore(it, InsnNode(POP2));
                                        }
                                    }

                                    notExecutedMethods.remove(lastFun)
                                }
                            }

                            instructions.insertBefore(it, endTry)

                            val label3 = LabelNode(Label())

                            instructions.insertBefore(it, JumpInsnNode(GOTO, label3))
                            instructions.insertBefore(it, startCatch)
                            instructions.insertBefore(it, VarInsnNode(ASTORE, 1))
                            instructions.insertBefore(it, VarInsnNode(ALOAD, 1))
                            instructions.insertBefore(it, MethodInsnNode(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false))
                            instructions.insertBefore(it, label3)
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
    }

    private fun initClassField(klass: ClassNode) {

        var isInit = false
        klass.methods.forEach { method ->
            if (method.isInitMethod) {
                val instructions = method.instructions
                method.instructions?.iterator()?.forEach {
                    if ((it.opcode >= Opcodes.IRETURN && it.opcode <= Opcodes.RETURN) || it.opcode == Opcodes.ATHROW) {
                        instructions.insertBefore(it, VarInsnNode(ALOAD, 0))

                        instructions.insertBefore(it, LdcInsnNode("def_from"))
                        instructions.insertBefore(it, FieldInsnNode(PUTFIELD, klass.name, FIELD_NAME_FROM, "Ljava/lang/String;"))
                        instructions.insertBefore(it, VarInsnNode(ALOAD, 0))
                        instructions.insertBefore(it, VarInsnNode(ALOAD, 0))
                        instructions.insertBefore(it, MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false))
                        instructions.insertBefore(
                            it,
                            MethodInsnNode(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false)
                        )
                        instructions.insertBefore(it, FieldInsnNode(PUTFIELD, klass.name, FIELD_NAME_FROM, "Ljava/lang/String;"))

                    }
                }
                isInit = true
            }
        }
        if (!isInit) {
//                createInitFunction(classNode, this)
            println("JunkCode <init> initClassField  does Not Exist")
        }
    }

    fun generateMethods(methodNames: ArrayList<String>, methodName: String, klass: ClassNode, notExecutedMethods: MutableMap<FunctionInfo, Int>) {
        if (!methodNames.contains(methodName)) {
            methodNames.add(methodName)
            val nextInt = random.nextInt(10000) % junkCodeMethods.size
            println("JunkCode generateMethods $nextInt add $methodName")

            junkCodeMethods[nextInt].insertJunkCode(methodName, klass, notExecutedMethods)
        }
    }

}