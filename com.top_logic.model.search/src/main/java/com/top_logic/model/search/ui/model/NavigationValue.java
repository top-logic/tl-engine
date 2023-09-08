/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.SpanResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.options.DefinitionsInScope;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * {@link RightHandSide} that produces a comparison value by navigating properties of some named
 * context object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(SpanResourceDisplay.class)
public interface NavigationValue extends RightHandSide, AbstractStep {

	/**
	 * Property name of {@link #getBase()}.
	 */
	String BASE = "base";

	/**
	 * Reference to some context object to start the navigation with.
	 */
	@Name(BASE)
	@Mandatory
	@Options(fun = DefinitionsInScope.class, args = { @Ref(EXPECTED_TYPE), @Ref(CONTEXT) })
	ReferenceValue getBase();

	@Override
	@DerivedRef({ BASE, ReferenceValue.VALUE_TYPE })
	TLType getValueType();

	@Override
	@DerivedRef({ BASE, ReferenceValue.VALUE_MULTIPLICITY })
	boolean getValueMultiplicity();

	@Override
	@DerivedRef({ CONTEXT, ValueContext.VALUE_TYPE })
	TLType getExpectedType();

	@Override
	@DerivedRef({ CONTEXT, ValueContext.VALUE_MULTIPLICITY })
	boolean getExpectedMultiplicity();

}
