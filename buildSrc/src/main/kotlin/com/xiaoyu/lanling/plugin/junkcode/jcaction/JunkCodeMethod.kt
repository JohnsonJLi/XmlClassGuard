package com.xiaoyu.lanling.plugin.junkcode.jcaction

import com.xiaoyu.lanling.plugin.junkcode.FunctionInfo
import org.objectweb.asm.tree.ClassNode
import java.util.LinkedHashMap

interface JunkCodeMethod {

    fun insertJunkCode(methodName: String, klass: ClassNode, notExecutedMethods: MutableMap<FunctionInfo, Int>)
}