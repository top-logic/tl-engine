/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;


/**
 * Sum interface for visiting all parts of a {@link AbstractQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface QueryVisitor<RQ, RE extends RQ, RS extends RQ, RF extends RQ, RO extends RQ, A> extends
		AbstractQueryVisitor<RQ, A>, ExpressionVisitor<RE, A>, SetExpressionVisitor<RS, A>, OrderVisitor<RO, A>,
		FunctionVisitor<RF, A> {

	// pure sum interface

}
