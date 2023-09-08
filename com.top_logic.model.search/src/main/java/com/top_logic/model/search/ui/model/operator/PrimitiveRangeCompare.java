/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.CustomOptionOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.ui.model.options.structure.RightHandSideOptions;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * {@link Operator} checking a primitive to be within a certain range.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface PrimitiveRangeCompare<I extends PrimitiveCompare.Impl<?>> extends PrimitiveCompare<I> {

	/**
	 * Property name of {@link #getLowerBound()}.
	 */
	String LOWER_BOUND = "lower-bound";

	/**
	 * Property name of {@link #getUpperBound()}.
	 */
	String UPPER_BOUND = "upper-bound";

	/**
	 * The lower bound of the range.
	 */
	@Name(LOWER_BOUND)
	@Mandatory
	@CustomOptionOrder
	@Options(fun = RightHandSideOptions.class, args = { @Ref(VALUE_TYPE), @Ref(VALUE_MULTIPLICITY) })
	RightHandSide getLowerBound();

	/**
	 * The upper bound of the range.
	 */
	@Name(UPPER_BOUND)
	@Mandatory
	@CustomOptionOrder
	@Options(fun = RightHandSideOptions.class, args = { @Ref(VALUE_TYPE), @Ref(VALUE_MULTIPLICITY) })
	RightHandSide getUpperBound();

}
