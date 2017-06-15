package com.jtransc.plugin.functor

import com.jtransc.ast.AstClass
import com.jtransc.ast.AstMethod
import com.jtransc.ast.AstType

val FUNCTIONAL_INTERFACE_REF = AstType.REF(FunctionalInterface::class.java.name)

fun AstClass?.getFunctionalInterface(): AstClass? {
	return (this?.allDirectInterfaces?.plus(this))?.firstOrNull {
		it.annotations.any {
			it.type == FUNCTIONAL_INTERFACE_REF
		}
	}
}

fun AstClass.getSAM(): AstMethod? = if (this.getFunctionalInterface() != null)
		this.methods.first { it.modifiers.isAbstract }
	else
		null

