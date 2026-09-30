/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;

/**
 * Annotation to set on a Java method that can be called from TL-Script, if the method has no side
 * effects.
 * 
 * <p>
 * A method without side effects does not modify any state: It neither creates, changes nor deletes
 * persistent objects, does not change the state of its arguments or of any other existing object,
 * and does not register or publish anything (e.g. in a service, a session, or a log). It may read
 * state, e.g. from the database, the session, or the current user, and it may create and return a
 * new object. The TL-Script compiler freely re-orders, drops, or duplicates calls to such a method.
 * </p>
 * 
 * <p>
 * {@link #canEvaluateAtCompileTime()} is only set for a pure function of its arguments: A method
 * whose result depends on nothing but its arguments (no access to the database, the session, the
 * current user, the current time, or random values) and whose result is immutable (e.g. a number or
 * a string, but not a structured text or a diagram node, which may be modified later on). A method
 * with a {@link UsesSecurity} parameter is never evaluated at compile time, since its result
 * depends on the current user.
 * </p>
 * 
 * @see #canEvaluateAtCompileTime()
 * @see GenericMethod#isSideEffectFree()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SideEffectFree {

	/**
	 * Flag to set to allow constant folding by the TL-Script compiler.
	 * 
	 * <p>
	 * When set to <code>true</code>, the method may be called at compile time, if all arguments are
	 * constants. The call is then replaced by its result. The method must therefore be a pure
	 * function of its arguments with an immutable result, since there is no {@link EvalContext} at
	 * compile time and the same result is used for every evaluation of the expression.
	 * </p>
	 * 
	 * @see GenericMethod#canEvaluateAtCompileTime(Object[])
	 */
	boolean canEvaluateAtCompileTime() default false;

}
