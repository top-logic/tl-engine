/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;



/**
 * Visitor interface for {@link Expression} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExpressionVisitor<R,A> {
	
	/**
	 * Visits {@link Literal}s.
	 */
	R visitLiteral(Literal expr, A arg);
	
	/**
	 * Visits {@link Parameter}s.
	 */
	R visitParameter(Parameter expr, A arg);
	
	/**
	 * Visits {@link Attribute}s.
	 */
	R visitAttribute(Attribute expr, A arg);

	/**
	 * Visits {@link UnaryOperation}s.
	 */
	R visitUnaryOperation(UnaryOperation expr, A arg);

	/**
	 * Visits {@link BinaryOperation}s.
	 */
	R visitBinaryOperation(BinaryOperation expr, A arg);

	/**
	 * Visits {@link HasType} expressions.
	 */
	R visitHasType(HasType expr, A arg);

	/**
	 * Visits {@link InstanceOf} expressions.
	 */
	R visitInstanceOf(InstanceOf expr, A arg);
	
	/**
	 * Visits {@link ExpressionTuple}s.
	 */
	R visitTuple(ExpressionTuple expr, A arg);

	R visitReference(Reference expr, A arg);

	R visitFlex(Flex expr, A arg);
	
	R visitGetEntry(GetEntry expr, A arg);

	R visitMatches(Matches expr, A arg);

	R visitEval(Eval expr, A arg);
	
	R visitContext(ContextAccess expr, A arg);
	
	R visitInSet(InSet expr, A arg);
	
	/**
	 * Visits {@link IsCurrent} expressions.
	 */
	R visitIsCurrent(IsCurrent expr, A arg);

	/**
	 * Visits {@link RequestedHistoryContext} expressions.
	 */
	R visitRequestedHistoryContext(RequestedHistoryContext expr, A arg);

}
