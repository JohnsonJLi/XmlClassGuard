package com.xml.guard.entensions

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

open class VariantExt(variant : NamedDomainObjectContainer<GuardExtension>) {

    var variantConfig: NamedDomainObjectContainer<GuardExtension> = variant

    fun variantConfig(action: Action<in NamedDomainObjectContainer<GuardExtension>>) {
        action.execute(variantConfig)
    }
}