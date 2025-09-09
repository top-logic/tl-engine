/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to add a {@link ValueConverter} to a method parameter.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ScriptConversion {

	/**
	 * The {@link ValueConverter} implementation class.
	 */
	Class<? extends ValueConverter> value();

}
