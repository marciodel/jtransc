package com.jtransc.plugin.functor

import com.jtransc.annotation.JTranscUnboxParam
import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.TreeShakingApi
import com.jtransc.plugin.JTranscPlugin
import com.jtransc.annotation.JTranscKeep

/**
 * Plugin to add all Functional Interfaces passed to or received from the target
 */
class FunctorJTranscPlugin : JTranscPlugin() {

	val functorSet by lazy {
		injector.get<FunctorSet>()
	}

	override val priority: Int
		get() = 0
//
//	override fun onAfterAllClassDiscovered(program: AstProgram): Unit {
//
//		for (clazz in program.classes) {
//			val functionalInterface = clazz.getFunctionalInterface()
//
//			if (functionalInterface != null) {
//				clazz.extraKeep = true
//				functionalInterface.extraKeep = true;
//
//				functorSet += clazz.name
//				functorSet += functionalInterface.name
//			}
//
//		}
//
//	}


	override fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass) {
		addIfFunctionalInterface(treeShaking, oldclass)
	}
	
	fun addIfFunctionalInterface(treeShaking: TreeShakingApi, clazz: AstClass){
		val functionalInterface = clazz.getFunctionalInterface()

		if (functionalInterface != null) {
			val sam = functionalInterface.getSAM()
			if(sam == null) return;

			treeShaking.addBasicClass(functionalInterface.name, "Functional Interface")
			treeShaking.addMethod(sam.ref, "Functional Interface")
			addTypeIfNativeOrFunctional(treeShaking, sam.type)
			sam.getParamsWithAnnotations().forEach { addTypeIfNativeOrFunctional(treeShaking, it.arg.type) }

			functorSet += functionalInterface.name

			if (clazz != functionalInterface) {
				val method = clazz.getMethod(sam.name, sam.desc)
				
				if(method != null){
					treeShaking.addMethod(method.ref, "Functional Interface")
				}

				functorSet += clazz.name
			}

		}		
	}

	fun addTypeIfNativeOrFunctional(treeShaking: TreeShakingApi, type: AstType) {
		if (!type.isREF()) return;

		val clazz = treeShaking.program[type.asREF()]
		if (clazz.isNativeForTarget(targetName)) {
			treeShaking.addBasicClass(type.asREF().name, "Functional Interface")
		}
		
		addIfFunctionalInterface(treeShaking, clazz!!) 
	}

	override fun onTreeShakingAddMethod(treeShakingApi: TreeShakingApi, oldmethod: AstMethod, newmethod: AstMethod) {
		val clazz = oldmethod.containingClass
		if (clazz.isNativeForTarget(targetName) ) {
			 treeShakingApi.addBasicClass(clazz.name, "Functional Interface")
//			&& (!oldmethod.hasBody || oldmethod.annotationsList.getBodiesForTarget(targetName).isNotEmpty() ||
//				oldmethod.annotationsList.getCallSiteBodyForTarget(targetName)?.isNotEmpty()?:false)
			oldmethod.getParamsWithAnnotations()
					.filter { it.arg.type.isREF() }
					.map { treeShakingApi.program[it.arg.type.asREF()].getFunctionalInterface() }
					.filterNotNull()
					.forEach {
						addIfFunctionalInterface(treeShakingApi, it)
					}
		}
	}


}