package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import org.objectweb.asm.tree.ClassNode

interface JunkCodeMethod {

    fun insertJunkCode(methodName: String, klass: ClassNode, notExecutedMethods: MutableList<FunctionInfo>)
}