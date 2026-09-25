/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.model.search.expr.EvalContext;

/**
 * Marks a {@link TLScriptFunctions} method, or all methods of a {@link TLScriptFunctions} class, as
 * a TL-Script function that only an administrator may call from a script entered interactively.
 *
 * <p>
 * A script a user types in, for example in the script console, is evaluated
 * {@link EvalContext#isInteractive() interactively}; there, a call of such a function by a user who
 * is not an administrator fails with a permission error. A script that is part of the application
 * configuration is not evaluated interactively and may call the function for every user.
 * </p>
 *
 * <p>
 * A function annotated this way depends on the current user and is therefore never evaluated at
 * compile time, even if it is annotated {@link SideEffectFree} with
 * {@link SideEffectFree#canEvaluateAtCompileTime()}.
 * </p>
 *
 * @see EvalContext#checkAdmin(String)
 * @see TLScriptMethod
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RUNTIME)
public @interface AdminOnly {

	// Pure marker annotation.

}
