/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Aggregation function.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Function extends QueryPart {

	public abstract <R, A> R visitFunction(FunctionVisitor<R, A> v, A arg);
	
	@Override
	public final <RQ, RE extends RQ,RS extends RQ,RF extends RQ,RO extends RQ,A> RQ visitQuery(QueryVisitor<RQ, RE,RS,RF,RO,A> v, A arg) {
		return visitFunction(v, arg);
	}

}
