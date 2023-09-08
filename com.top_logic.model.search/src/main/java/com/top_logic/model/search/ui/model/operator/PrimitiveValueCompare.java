/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.search.ui.model.options.structure.RightHandSideOptions;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * {@link Operator} for comparing {@link TLPrimitive} values with a single other
 * {@link #getComparisonValue()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Abstract
public interface PrimitiveValueCompare<I extends PrimitiveCompare.Impl<?>> extends PrimitiveCompare<I> {

	/**
	 * Property name of {@link #getComparisonValue()}.
	 */
	String COMPARISON_VALUE = "comparison-value";

	/**
	 * Specification of right hand side value used for comparison.
	 */
	@Name(COMPARISON_VALUE)
	@Mandatory
	@CustomOptionOrder
	@Options(fun = RightHandSideOptions.class, args = { @Ref(VALUE_TYPE), @Ref(VALUE_MULTIPLICITY) })
	RightHandSide getComparisonValue();

}
