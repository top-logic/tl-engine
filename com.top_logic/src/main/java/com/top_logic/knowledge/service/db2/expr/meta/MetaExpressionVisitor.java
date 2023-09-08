/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.meta;

import com.top_logic.knowledge.search.ExpressionVisitor;

/**
 * {@link ExpressionVisitor} that also visits the {@link MetaValue}s.
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public interface MetaExpressionVisitor<R,A> extends ExpressionVisitor<R, A> {

	/**
	 * Visits the given {@link MetaValue} with this visitor.
	 */
	R visitMetaValue(MetaValue mexpr, A arg);
	
}
