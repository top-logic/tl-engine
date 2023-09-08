/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Base interface for models representing a floating-point-number comparisons.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractFloatCompare<I extends PrimitiveCompare.Impl<?>> extends PrimitiveCompare<I> {

	/**
	 * Property name of {@link #getPrecision()}.
	 */
	String PRECISION = "precision";

	/**
	 * The number of decimal digits after the dot that are relevant for the comparison.
	 */
	@IntDefault(2)
	@Name(PRECISION)
	int getPrecision();

}
