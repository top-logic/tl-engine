/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;



/**
 * Visitor interface for the {@link SetExpression} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SetExpressionVisitor<R,A> {

	/**
	 * Visit the given {@link None} with this visitor.
	 */
	R visitNone(None expr, A arg);
	
	/**
	 * Visit the given {@link SetLiteral} with this visitor.
	 */
	R visitSetLiteral(SetLiteral expr, A arg);

	/**
	 * Visit the given {@link SetParameter} with this visitor.
	 */
	R visitSetParameter(SetParameter expr, A arg);
	
	/**
	 * Visit the given {@link AllOf} with this visitor.
	 */
	R visitAllOf(AllOf expr, A arg);

	/**
	 * Visit the given {@link AllOf} with this visitor.
	 */
	R visitAnyOf(AnyOf expr, A arg);
	
	/**
	 * Visit the given {@link Substraction} with this visitor.
	 */
	R visitSubstraction(Substraction expr, A arg);

	/**
	 * Visit the given {@link Intersection} with this visitor.
	 */
	R visitIntersection(Intersection expr, A arg);

	/**
	 * Visit the given {@link Union} with this visitor.
	 */
	R visitUnion(Union expr, A arg);

	/**
	 * Visit the given {@link CrossProduct} with this visitor.
	 */
	R visitCrossProduct(CrossProduct expr, A arg);

	/**
	 * Visit the given {@link Filter} with this visitor.
	 */
	R visitFilter(Filter expr, A arg);

	/**
	 * Visit the given {@link MapTo} with this visitor.
	 */
	R visitMapTo(MapTo expr, A arg);

	/**
	 * Visit the given {@link Partition} with this visitor.
	 */
	R visitPartition(Partition expr, A arg);

}
