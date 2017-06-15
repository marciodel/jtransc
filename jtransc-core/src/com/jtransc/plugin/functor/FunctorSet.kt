package com.jtransc.plugin.functor

import com.jtransc.ast.FqName
import com.jtransc.injector.Singleton

/**
 * Holds the list of identified Functional Interfaces passed to or received from the target
 */
@Singleton
class FunctorSet : ArrayList<FqName>()
