package com.xiaoyu.lanling.plugin.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodNode


const val DESC_STRING = "Ljava/lang/String;"
const val DESC_BOOLEAN = "Z"
const val DESC_INT = "I"

val MethodNode.nameWithDesc: String
    get() = name + desc

val MethodNode.isStatic: Boolean
    get() = access and Opcodes.ACC_STATIC != 0

val MethodNode.isInitMethod: Boolean
    get() = name == "<init>"

val MethodNode.isClInitMethod: Boolean
    get() = name == "<clinit>"

val MethodNode.isAbstractMethod: Boolean
    get() = access and Opcodes.ACC_ABSTRACT == Opcodes.ACC_ABSTRACT

val MethodNode.isNativeMethod: Boolean
    get() = access and Opcodes.ACC_NATIVE == Opcodes.ACC_NATIVE

val MethodNode.isEmptyMethod: Boolean
    get() = maxStack == 0 || (maxStack == 1 && isInitMethod)

val MethodNode.isGetAndSetFieldMethod: Boolean
    get() = (maxStack == 1 && desc.startsWith("()")
            || maxStack <= 2 && desc.endsWith(";)V") && desc.indexOf(';') == desc.lastIndexOf(';'))
            && instructions.let {
        it?.iterator()?.forEach { node ->
            if (node is FieldInsnNode) return@let true
        }
        return@let false
    }

val String.isStringDescriptor: Boolean
    get() = this == DESC_STRING

val String.isBooleanDescriptor: Boolean
    get() = this == DESC_BOOLEAN

val String.isIntDescriptor: Boolean
    get() = this == DESC_INT
