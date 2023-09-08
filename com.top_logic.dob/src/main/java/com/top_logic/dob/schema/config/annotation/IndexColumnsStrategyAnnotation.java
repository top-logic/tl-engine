/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config.annotation;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.dob.meta.MOAnnotation;

/**
 * {@link MOAnnotation} adding an {@link IndexColumnsStrategy} to a type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IndexColumnsStrategyAnnotation extends MOAnnotation {

	/**
	 * The {@link IndexColumnsStrategy} for the annotated type and its subclasses that doe not
	 * define their own strategy.
	 */
	@InstanceFormat
	IndexColumnsStrategy getStrategy();

	/**
	 * @see #getStrategy()
	 */
	void setStrategy(IndexColumnsStrategy value);

}
