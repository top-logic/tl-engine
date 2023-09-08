/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.CustomOptionOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.ui.model.operator.Operator;
import com.top_logic.model.search.ui.model.options.structure.OperatorOptions;

/**
 * Combination of {@link Operator}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface OperatorContainer extends ValueContext {

	/**
	 * Property name of {@link #getComparisons()}.
	 */
	String COMPARISONS = "comparisons";

	/**
	 * The {@link Operator}s to execute on the property value.
	 */
	@CustomOptionOrder
	@Options(fun = OperatorOptions.class, args = {
		@Ref(VALUE_TYPE),
		@Ref(VALUE_MULTIPLICITY),
		@Ref(CONFIG_NAME)
	})
	@Name(COMPARISONS)
	Collection<? extends Operator<?>> getComparisons();

}
