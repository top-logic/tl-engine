/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.meta;

import com.top_logic.knowledge.search.SetExpressionVisitor;

/**
 * {@link SetExpressionVisitor} that also visits the {@link MetaSet}s.
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public interface MetaSetExpressionVisitor<R,A> extends SetExpressionVisitor<R, A> {

	/**
	 * visits the given {@link MetaSet} with this visitor.
	 */
	R visitMetaSet(MetaSet mexpr, A arg);
	
}
