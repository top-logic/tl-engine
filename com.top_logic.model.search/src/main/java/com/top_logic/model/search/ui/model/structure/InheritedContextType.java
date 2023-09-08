/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.structure;

import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.ValueContext;

/**
 * {@link ValueContext} models that use the {@link #getValueType()} of the {@link #getContext()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface InheritedContextType extends ValueContext, WithValueContext {

	@Override
	@DerivedRef(WithValueContext.CONTEXT_TYPE)
	TLType getValueType();

	@Override
	@DerivedRef(WithValueContext.CONTEXT_MULTIPLICITY)
	boolean getValueMultiplicity();

}
