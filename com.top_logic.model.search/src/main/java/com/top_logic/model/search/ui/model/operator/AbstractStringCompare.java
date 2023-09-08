/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;

/**
 * Base interface for models representing {@link String} comparisons.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractStringCompare<I extends PrimitiveCompare.Impl<?>> extends PrimitiveCompare<I> {

	/**
	 * Property name of {@link #isCaseSensitive()}.
	 */
	String CASE_SENSITIVE = "case-sensitive";

	/**
	 * Whether the comparison should consider upper and lower case letters different.
	 */
	@Name(CASE_SENSITIVE)
	boolean isCaseSensitive();

	@Override
	@Derived(fun = AbstractStringCompare.StringType.class, args = {})
	TLType getValueType();

	/**
	 * Function implementing {@link AbstractStringCompare#getValueType()}.
	 */
	class StringType extends PrimitiveType {

		@Override
		protected TLPrimitive.Kind getKind() {
			return TLPrimitive.Kind.STRING;
		}
	
	}

}
