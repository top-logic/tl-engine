/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.model.search.expr.GenericMethod;

/**
 * Annotation to set on a Java method that can be called from TL-Script, if the method has no side
 * effects.
 * 
 * @see #canEvaluateAtCompileTime()
 * @see GenericMethod#isSideEffectFree()
 */
public @interface SideEffectFree {

	/**
	 * Flag to set to allow constant folding by the TL-Script compiler.
	 * 
	 * <p>
	 * When set to <code>true</code>, the method may be called at compile time, if all arguments are
	 * constants.
	 * </p>
	 * 
	 * @see GenericMethod#canEvaluateAtCompileTime(Object[])
	 */
	boolean canEvaluateAtCompileTime() default false;

}
