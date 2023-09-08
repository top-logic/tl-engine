/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.expr;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.persistency.attribute.AbstractExprMapping;

/**
 * {@link StorageMapping} for storing search expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExprStorageMapping extends AbstractExprMapping {

	/**
	 * Singleton {@link ExprStorageMapping} instance.
	 */
	public static final ExprStorageMapping INSTANCE = new ExprStorageMapping();

	private ExprStorageMapping() {
		// Singleton constructor.
	}

	@Override
	protected SearchExpression compile(String source) throws ConfigurationException {
		return compile(source, ExprFormat.INSTANCE.getValue(null, source));
	}

}
