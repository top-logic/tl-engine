/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.form.values.edit.annotation.CustomOptionOrder;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.ui.model.combinator.AllSearchExpressionCombinator;
import com.top_logic.model.search.ui.model.combinator.SearchExpressionCombinator;
import com.top_logic.model.search.ui.model.options.ConfiguredSearchExpressionCombinators;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * A {@link SearchPart} that combines multiple subexpressions with a
 * {@link SearchExpressionCombinator}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Abstract
public interface CombinationExpression extends SearchPart {

	/**
	 * Property name of {@link #getCombinator()}.
	 */
	String COMBINATOR = "combinator";

	/**
	 * The {@link SearchExpressionCombinator} defines how the expressions should be combined into a
	 * single expression.
	 */
	@NonNullable
	@CustomOptionOrder
	@Options(fun = ConfiguredSearchExpressionCombinators.class, args = @Ref(CONFIG_NAME) )
	@InstanceFormat
	@InstanceDefault(AllSearchExpressionCombinator.class)
	@ItemDisplay(ItemDisplayType.VALUE)
	@Name(COMBINATOR)
	SearchExpressionCombinator getCombinator();

}
