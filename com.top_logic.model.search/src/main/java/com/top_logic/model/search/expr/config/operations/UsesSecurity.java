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

import com.top_logic.model.search.WithSecurityCheck;

/**
 * Marks a <code>boolean</code> parameter of a {@link TLScriptFunctions} method that receives
 * whether the script is evaluated with the current user's access rights.
 *
 * <p>
 * The annotated parameter is not an argument of the TL-Script function: A script cannot pass a
 * value for it, and it is not part of the function's documentation. Instead, each call receives
 * the {@link WithSecurityCheck#usesSecurity() security flag} of the calling expression. The flag is
 * <code>true</code>, unless the security check was switched off for the whole script.
 * </p>
 *
 * <p>
 * A function with such a parameter depends on the current user and is therefore never evaluated at
 * compile time, even if it is annotated {@link SideEffectFree} with
 * {@link SideEffectFree#canEvaluateAtCompileTime()}.
 * </p>
 *
 * @see TLScriptMethod
 */
@Target(ElementType.PARAMETER)
@Retention(RUNTIME)
public @interface UsesSecurity {

	// Pure marker annotation.

}
