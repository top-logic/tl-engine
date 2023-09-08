/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.structure;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.ValueContext;

/**
 * Base interface for form input models that can only exist within a {@link ValueContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface WithValueContext extends SearchPart {

	/**
	 * Property name of {@link #getContextType()}.
	 */
	String CONTEXT_TYPE = "context-type";

	/**
	 * Property name of {@link #getContextMultiplicity()}.
	 */
	String CONTEXT_MULTIPLICITY = "context-multiplicity";

	/**
	 * The {@link ValueContext} this {@link SearchPart} lives in.
	 */
	@Override
	ValueContext getContext();

	/**
	 * {@link ValueContext#getValueType()} of {@link #getContext()}.
	 */
	@Name(CONTEXT_TYPE)
	@DerivedRef({ WithValueContext.CONTEXT, ValueContext.VALUE_TYPE })
	TLType getContextType();

	/**
	 * {@link ValueContext#getValueMultiplicity()} of {@link #getContext()}.
	 */
	@Name(CONTEXT_MULTIPLICITY)
	@DerivedRef({ WithValueContext.CONTEXT, ValueContext.VALUE_MULTIPLICITY })
	boolean getContextMultiplicity();

}
