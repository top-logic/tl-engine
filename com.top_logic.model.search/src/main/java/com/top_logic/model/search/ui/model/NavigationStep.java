/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.structure.WithValueContext;

/**
 * A single step in a {@link NavigationValue} expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface NavigationStep extends WithValueContext, AbstractStep {

	@Override
	AbstractStep getContext();

	@Override
	@DerivedRef({ CONTEXT, AbstractStep.EXPECTED_TYPE })
	TLType getExpectedType();

	@Override
	@DerivedRef({ CONTEXT, AbstractStep.EXPECTED_MULTIPLICITY })
	boolean getExpectedMultiplicity();

}
