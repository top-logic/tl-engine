/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.CustomOptionOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.model.TLType;
import com.top_logic.model.search.ui.model.function.VisibleIfNavigable;
import com.top_logic.model.search.ui.model.options.NavigationOptions;

/**
 * Base interface for {@link NavigationValue} parts.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractStep extends ValueContext {

	/**
	 * Property name of {@link #getExpectedType()}.
	 */
	String EXPECTED_TYPE = "expected-type";

	/**
	 * Property name of {@link #getExpectedMultiplicity()}.
	 */
	String EXPECTED_MULTIPLICITY = "expected-multiplicity";

	/**
	 * Property name of {@link #getNext()}.
	 */
	String NEXT = "next";

	/**
	 * The expected type of the complete {@link NavigationValue} expression, this
	 * {@link AbstractStep} is part of.
	 */
	@Abstract
	@Hidden
	@Name(EXPECTED_TYPE)
	TLType getExpectedType();

	/**
	 * The expected multiplicity of the complete {@link NavigationValue} expression, this
	 * {@link AbstractStep} is part of.
	 */
	@Abstract
	@Hidden
	@Name(EXPECTED_MULTIPLICITY)
	boolean getExpectedMultiplicity();

	/**
	 * The next (optional) navigation step.
	 */
	@Name(NEXT)
	@DynamicMode(fun = VisibleIfNavigable.class, args = {
		@Ref(VALUE_TYPE),
		@Ref(CONFIG_NAME)
	})
	@CustomOptionOrder
	@Options(fun = NavigationOptions.class, args = {
		@Ref(VALUE_TYPE),
		@Ref(EXPECTED_MULTIPLICITY),
		@Ref(CONFIG_NAME)
	})
	NavigationStep getNext();

}
