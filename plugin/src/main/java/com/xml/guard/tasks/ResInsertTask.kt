package com.xml.guard.tasks

import org.gradle.api.tasks.Exec

open class ResInsertTask : Exec() {

    init {
        commandLine("python", "res_insert.py" )
    }
}