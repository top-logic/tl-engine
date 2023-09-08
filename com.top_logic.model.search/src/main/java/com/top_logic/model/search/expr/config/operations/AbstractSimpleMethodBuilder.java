/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link MethodBuilder} without additional configuration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractSimpleMethodBuilder<I extends SearchExpression>
		extends AbstractMethodBuilder<AbstractMethodBuilder.Config<?>, I> {

	/**
	 * Creates a {@link AbstractSimpleMethodBuilder}.
	 */
	public AbstractSimpleMethodBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

}
