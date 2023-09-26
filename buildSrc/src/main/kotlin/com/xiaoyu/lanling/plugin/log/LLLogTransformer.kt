package com.xiaoyu.lanling.plugin.log

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import com.xiaoyu.lanling.plugin.utils.*
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

//@AutoService(ClassTransformer::class)
class LLLogTransformer : ClassTransformer {

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (!ClassUtils.checkClassName(klass.name)
            || !(klass.name.startsWith("com/xiaoyu/lanling")
                    || klass.name.startsWith("com/xiaoyu/im"))


            || klass.name.startsWith("com/xiaoyu/lanling/view")
            || klass.name.startsWith("com/xiaoyu/lanling/widget")
            || klass.name.startsWith("com/xiaoyu/lanling/util")
            || klass.name.startsWith("com/xiaoyu/lanling/net")
            || klass.name.startsWith("com/xiaoyu/lanling/media")
            || klass.name.startsWith("com/xiaoyu/lanling/event")
            || klass.name.startsWith("com/xiaoyu/lanling/entity")


//            || klass.name.startsWith("com/xiaoyu/common_utils")
//            || klass.name.startsWith("com/xiaoyu/camera")
//            || klass.name.startsWith("com/xiaoyu/facedetector")
//            || klass.name.startsWith("com/xiaoyu/lanling/media")
//            || klass.name.startsWith("com/xiaoyu/log")
//            || klass.name.startsWith("com/xiaoyu/base/net")
//            || klass.name.startsWith("com/xiaoyu/base/utils")
//            || klass.name.startsWith("com/xiaoyu/base/view")
//            || klass.name.startsWith("com/xiaoyu/storage_base")

            || klass.name.contains("AppBlockCanaryContext")
            || klass.name.contains("AnimData")
            || klass.name.contains("ImageLoader")
            || klass.name.contains("SimpleTask")
            || klass.name.contains("RequestBase")
            || klass.name.contains("RequestData")
            || klass.name.endsWith("Binding")
            || klass.name.endsWith("Bean")
            || klass.name.endsWith("Entity")
            || klass.name.endsWith("Item")

            || klass.name.endsWith("View")
            || klass.superName.endsWith("View")
            || klass.name.endsWith("Layout")
            || klass.superName.endsWith("Layout")
            || klass.superName.endsWith("ViewGroup")
            || klass.superName.endsWith("Adapter")
        ) return klass
        println("LLLog Transforming ${klass.name}")
        klass.methods?.filter {
            !it.isAbstractMethod && !it.isNativeMethod && !it.isEmptyMethod && !it.isGetAndSetFieldMethod
                    && !it.name.endsWith("toString")
                    && !it.name.endsWith("equals")
                    && !it.name.endsWith("hashCode")
                    && !it.name.contains("draw")
                    && !it.name.contains("Draw")
                    && !it.name.contains("layout")
                    && !it.name.contains("Layout")
                    && !it.name.contains("measure")
                    && !it.name.contains("Measure")
                    && !it.name.contains("getInstance")
        }?.forEach { method ->
            method.insertMethodLogTrack3(klass)
        }
        return klass
    }


    private fun MethodNode.insertMethodTimeLogTrack2(classNode: ClassNode) {
        val isStaticMethod = isStatic

        val methodType: Type = Type.getMethodType(desc)


        var methodLineNumber: Int? = null
        instructions?.iterator()?.forEach {
            if (methodLineNumber == null && it is LineNumberNode) {
                methodLineNumber = it.line
                return@forEach
            }
        }
        instructions?.apply {
            val firstNode = first ?: return
            insertBefore(firstNode, LabelNode(Label()))
            insertBefore(firstNode, LdcInsnNode("full_stack"))
            insertBefore(firstNode, LdcInsnNode("enter"))
            insertBefore(
                firstNode,
                LdcInsnNode(
                    "${classNode.name.replace("/", ".")}.$name(${classNode.sourceFile}:${methodLineNumber ?: -1})"
                )
            )

            insertBefore(
                firstNode,
                MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "com/xiaoyu/log/clog/CLog",
                    "i",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                    false
                )
            )
//            insertBefore(firstNode, InsnNode(Opcodes.POP))

//           end
            insertBefore(firstNode, LabelNode(Label()))

            iterator().forEach {
                if (it is LineNumberNode) {
                    methodLineNumber = it.line
                }
                if ((it.opcode in Opcodes.IRETURN..Opcodes.RETURN) || it.opcode == Opcodes.ATHROW) {
                    // 2022/5/10 return  获取返回结果
                    when (it.opcode) {
                        in Opcodes.IRETURN..Opcodes.DRETURN -> {
                            val returnType = methodType.returnType
                            val size = returnType.size
                            val descriptor = returnType.descriptor
                            if (size == 1) {
                                insertBefore(it, InsnNode(Opcodes.DUP))
                            } else {
                                insertBefore(it, InsnNode(Opcodes.DUP2))
                            }
                            insertBefore(
                                it,
                                MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "java/lang/String",
                                    "valueOf",
                                    String.format("(%s)Ljava/lang/String;", descriptor),
                                    false
                                )
                            )
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                        Opcodes.ARETURN -> {
                            insertBefore(it, InsnNode(Opcodes.DUP))
                            insertBefore(
                                it,
                                MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "java/lang/String",
                                    "valueOf",
                                    "(Ljava/lang/Object;)Ljava/lang/String;",
                                    false
                                )
                            )
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                        Opcodes.RETURN -> {
                            insertBefore(it, LdcInsnNode("void"))
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                        else -> {
                            insertBefore(it, LdcInsnNode("abnormal"))
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                    }
                    //return end

                    insertBefore(it, LabelNode(Label()))

                    insertBefore(it, LdcInsnNode("full_stack"))
                    insertBefore(it, LdcInsnNode("end"))
                    insertBefore(it, TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"))
                    insertBefore(it, InsnNode(Opcodes.DUP))
                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            "java/lang/StringBuilder",
                            "<init>",
                            "()V",
                            false
                        )
                    )
                    insertBefore(
                        it,
                        LdcInsnNode(
                            "${
                                classNode.name.replace(
                                    "/",
                                    "."
                                )
                            }.$name(${classNode.sourceFile}:${methodLineNumber ?: -1}) "
                        )
                    )
                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                            false
                        )
                    )

                    // 2022/5/10 arguments
                    var slotIndex = if (isStaticMethod) 0 else 1

                    val argumentTypes: Array<Type> = methodType.argumentTypes
                    if (argumentTypes.isNotEmpty()) {
                        insertBefore(it, LdcInsnNode(" args("))
                        insertBefore(
                            it,
                            MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/StringBuilder",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                            )
                        )

                        for (t in argumentTypes) {
                            val sort: Int = t.sort
                            val size: Int = t.size
                            val descriptor: String = t.descriptor
                            val opcode: Int = t.getOpcode(Opcodes.ILOAD)
                            insertBefore(
                                it,
                                LdcInsnNode("${if (slotIndex > (if (isStaticMethod) 0 else 1)) ";  " else ""} arg$slotIndex : ")
                            )
                            insertBefore(
                                it,
                                MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    "java/lang/StringBuilder",
                                    "append",
                                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                    false
                                )
                            )
                            insertBefore(it, VarInsnNode(opcode, slotIndex))

                            if (sort >= Type.BOOLEAN && sort <= Type.DOUBLE) {
                                insertBefore(
                                    it,
                                    MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL,
                                        "java/lang/StringBuilder",
                                        "append",
                                        String.format(
                                            "(%s)Ljava/lang/StringBuilder;",
                                            descriptor
                                        ),
                                        false
                                    )
                                )
                            } else {
                                insertBefore(
                                    it,
                                    MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL,
                                        "java/lang/StringBuilder",
                                        "append",
                                        String.format(
                                            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                                            descriptor
                                        ),
                                        false
                                    )
                                )
                            }
                            slotIndex += size
                        }

                        insertBefore(it, LdcInsnNode(" )  return : "))
                        insertBefore(
                            it,
                            MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/StringBuilder",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                            )
                        )

                        // 2022/5/10 return log
                        insertBefore(it, VarInsnNode(Opcodes.ALOAD, 6543))
                        insertBefore(
                            it,
                            MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/StringBuilder",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                            )
                        )
                    }

                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "toString",
                            "()Ljava/lang/String;",
                            false
                        )
                    )

                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/xiaoyu/log/clog/CLog",
                            "i",
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                            false
                        )
                    )
//                    insertBefore(it, InsnNode(Opcodes.POP))
                    insertBefore(firstNode, LabelNode(Label()))
                }
            }
        }
    }

    private fun MethodNode.insertMethodLogTrack3(classNode: ClassNode) {
        val isStaticMethod = isStatic

        val methodType: Type = Type.getMethodType(desc)


        var methodLineNumber: Int? = null
        instructions?.iterator()?.forEach {
            if (methodLineNumber == null && it is LineNumberNode) {
                methodLineNumber = it.line
                return@forEach
            }
        }
        instructions?.apply {
            val firstNode = first ?: return

            insertBefore(firstNode, LabelNode(Label()))

            insertBefore(firstNode, LdcInsnNode("full_stack"))

            insertBefore(firstNode, LdcInsnNode("enter"))

            insertBefore(
                firstNode,
                LdcInsnNode(
                    "${
                        classNode.name.replace(
                            "/",
                            "."
                        )
                    }.$name(${classNode.sourceFile}:${methodLineNumber ?: -1})"
                )
            )

            insertBefore(
                firstNode,
                MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "com/xiaoyu/log/clog/CLog",
                    "i",
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                    false
                )
            )
//            insertBefore(firstNode, InsnNode(Opcodes.POP))

            insertBefore(firstNode, LabelNode(Label()))
//           end

            iterator().forEach {
                if (it is LineNumberNode) {
                    methodLineNumber = it.line
                }
                if ((it.opcode in Opcodes.IRETURN..Opcodes.RETURN) || it.opcode == Opcodes.ATHROW) {
                    // 2022/5/10 return  获取返回结果
                    when (it.opcode) {
                        in Opcodes.IRETURN..Opcodes.DRETURN -> {
                            val returnType = methodType.returnType
                            val size = returnType.size
                            val descriptor = returnType.descriptor
                            if (size == 1) {
                                insertBefore(it, InsnNode(Opcodes.DUP))
                            } else {
                                insertBefore(it, InsnNode(Opcodes.DUP2))
                            }
                            insertBefore(
                                it,
                                MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "java/lang/String",
                                    "valueOf",
                                    String.format("(%s)Ljava/lang/String;", if (returnType.sort == Type.BYTE) "I" else descriptor),
                                    false
                                )
                            )
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                        Opcodes.ARETURN -> {
                            insertBefore(it, InsnNode(Opcodes.DUP))
                            insertBefore(
                                it,
                                MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "java/lang/String",
                                    "valueOf",
                                    "(Ljava/lang/Object;)Ljava/lang/String;",
                                    false
                                )
                            )
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                        Opcodes.RETURN -> {
                            insertBefore(it, LdcInsnNode("void"))
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                        else -> {
                            insertBefore(it, LdcInsnNode("abnormal"))
                            insertBefore(it, VarInsnNode(Opcodes.ASTORE, 6543))
                        }
                    }
                    //return end

                    insertBefore(it, LabelNode(Label()))

                    insertBefore(it, LdcInsnNode("full_stack"))
                    insertBefore(it, LdcInsnNode("end"))
                    insertBefore(it, TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"))
                    insertBefore(it, InsnNode(Opcodes.DUP))
                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            "java/lang/StringBuilder",
                            "<init>",
                            "()V",
                            false
                        )
                    )
                    insertBefore(
                        it,
                        LdcInsnNode(
                            "${
                                classNode.name.replace(
                                    "/",
                                    "."
                                )
                            }.$name(${classNode.sourceFile}:${methodLineNumber ?: -1}) "
                        )
                    )
                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                            false
                        )
                    )

                    // 2022/5/10 arguments
                    var slotIndex = if (isStaticMethod) 0 else 1

                    val argumentTypes: Array<Type> = methodType.argumentTypes
                    if (argumentTypes.isNotEmpty()) {
                        insertBefore(it, LdcInsnNode(" args("))
                        insertBefore(
                            it,
                            MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/StringBuilder",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                            )
                        )

                        for (t in argumentTypes) {
                            val sort: Int = t.sort
                            val size: Int = t.size
                            val descriptor: String = t.descriptor
                            val opcode: Int = t.getOpcode(Opcodes.ILOAD)
                            insertBefore(
                                it,
                                LdcInsnNode("${if (slotIndex > (if (isStaticMethod) 0 else 1)) ";  " else ""} arg$slotIndex : ")
                            )
                            insertBefore(
                                it,
                                MethodInsnNode(
                                    Opcodes.INVOKEVIRTUAL,
                                    "java/lang/StringBuilder",
                                    "append",
                                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                    false
                                )
                            )
                            insertBefore(it, VarInsnNode(opcode, slotIndex))

                            if (sort >= Type.BOOLEAN && sort <= Type.DOUBLE) {
                                insertBefore(
                                    it,
                                    MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL,
                                        "java/lang/StringBuilder",
                                        "append",
                                        String.format(
                                            "(%s)Ljava/lang/StringBuilder;",
                                            if (sort == Type.BYTE) "I" else descriptor
                                        ),
                                        false
                                    )
                                )
                            } else {
                                insertBefore(
                                    it,
                                    MethodInsnNode(
                                        Opcodes.INVOKEVIRTUAL,
                                        "java/lang/StringBuilder",
                                        "append",
                                        String.format(
                                            "(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                                            descriptor
                                        ),
                                        false
                                    )
                                )
                            }
                            slotIndex += size
                        }

                        insertBefore(it, LdcInsnNode(" )  return : "))
                        insertBefore(
                            it,
                            MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/StringBuilder",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                            )
                        )

                        // 2022/5/10 return log
                        insertBefore(it, VarInsnNode(Opcodes.ALOAD, 6543))
                        insertBefore(
                            it,
                            MethodInsnNode(
                                Opcodes.INVOKEVIRTUAL,
                                "java/lang/StringBuilder",
                                "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                                false
                            )
                        )
                    }


                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "toString",
                            "()Ljava/lang/String;",
                            false
                        )
                    )

                    insertBefore(
                        it,
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/xiaoyu/log/clog/CLog",
                            "i",
                            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                            false
                        )
                    )
//                    insertBefore(it, InsnNode(Opcodes.POP))
                    insertBefore(it, LabelNode(Label()))
                }
            }
        }
    }

}