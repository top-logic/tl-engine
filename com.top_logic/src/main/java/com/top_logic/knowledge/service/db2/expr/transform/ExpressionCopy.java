/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform;

import com.top_logic.knowledge.search.QueryPart;

/**
 * BHU: This class
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ExpressionCopy extends ExpressionTransformer<Void> {
	
	/**
	 * Singleton {@link ExpressionCopy} instance.
	 */
	public static final ExpressionCopy INSTANCE = new ExpressionCopy();
	
	private static final Void none = null;

	private ExpressionCopy() {
		// Singleton constructor.
	}

	@SuppressWarnings("unchecked")
	public static <E extends QueryPart> E copy(E expr) {
		return (E) expr.visitQuery(INSTANCE, none);
	}
	
}