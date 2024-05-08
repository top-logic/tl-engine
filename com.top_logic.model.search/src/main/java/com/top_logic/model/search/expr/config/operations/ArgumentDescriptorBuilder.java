/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.col.Provider;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;

/**
 * Algorithm creating a custom {@link ArgumentDescriptor}.
 * 
 * @see ArgumentDescriptor#builder()
 * @see ArgumentDescriptorImpl#ANY
 */
public interface ArgumentDescriptorBuilder {

	/**
	 * Creates the final {@link ArgumentDescriptor}.
	 */
	ArgumentDescriptor build();

	/**
	 * Creates a named parameter.
	 */
	ArgumentDescriptorBuilder mandatory(String name);

	/**
	 * Creates an optional named parameter with <code>null</code> as default value.
	 */
	default ArgumentDescriptorBuilder optional(String name) {
		return optional(name, () -> SearchExpressionFactory.literal(null));
	}

	/**
	 * Creates an optional named parameter with a boolean default value.
	 */
	default ArgumentDescriptorBuilder optional(String name, boolean defaultValue) {
		return optional(name, () -> SearchExpressionFactory.literal(Boolean.valueOf(defaultValue)));
	}

	/**
	 * Creates an optional named parameter with an integer default.
	 */
	default ArgumentDescriptorBuilder optional(String name, int defaultValue) {
		return optional(name, () -> SearchExpressionFactory.literal(SearchExpression.toNumber(defaultValue)));
	}

	/**
	 * Creates an optional named parameter with a string default.
	 */
	default ArgumentDescriptorBuilder optional(String name, String defaultValue) {
		return optional(name, () -> SearchExpressionFactory.literal(defaultValue));
	}

	/**
	 * Creates an optional named parameter.
	 * <p>
	 * The {@link Provider} is evaluated when the script is compiled.
	 * </p>
	 */
	ArgumentDescriptorBuilder optional(String name, Provider<SearchExpression> defaultValue);

}
