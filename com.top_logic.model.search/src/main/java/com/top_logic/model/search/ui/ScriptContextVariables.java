/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an {@link com.top_logic.model.search.expr.config.dom.Expr} configuration property with a
 * {@link ScriptContextVariablesProvider} whose variables are offered at the top level of
 * <code>$</code>-completion.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScriptContextVariables {

	/**
	 * The provider computing the context variable names.
	 */
	Class<? extends ScriptContextVariablesProvider> value();

}
