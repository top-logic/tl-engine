/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Visitor interface for {@link Operator}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface OperatorVisitor<R,A> {

	/**
	 * Visits a {@link Operator#EQBINARY} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitEqBinary(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#EQCI} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitEqCi(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#GT} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitGt(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#LT} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitLt(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#GE} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitGe(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#LE} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitLe(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#AND} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitAnd(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#OR} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitOr(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#NOT} operator.
	 * 
	 * @param operator
	 *        The visited operator.
	 * @param arg
	 *        The visit argument.
	 * @return The visit result.
	 */
	R visitNot(Operator operator, A arg);

	/**
	 * Visits a {@link Operator#IS_NULL} operator.
	 * 
	 * @param arg
	 *        The visit argument.
	 * 
	 * @return The visit result.
	 */
	R visitIsNull(A arg);

	/**
	 * Visits a {@link Operator#REVISION} operator.
	 * 
	 * @param arg
	 *        The visit argument.
	 * 
	 * @return The visit result.
	 */
	R visitRevision(A arg);

	/**
	 * Visits a {@link Operator#BRANCH} operator.
	 * 
	 * @param arg
	 *        The visit argument.
	 * 
	 * @return The visit result.
	 */
	R visitBranch(A arg);

	/**
	 * Visits a {@link Operator#TYPE_NAME} operator.
	 * 
	 * @param arg
	 *        The visit argument.
	 * 
	 * @return The visit result.
	 */
	R visitType(A arg);

	/**
	 * Visits a {@link Operator#IDENTIFIER} operator.
	 * 
	 * @param arg
	 *        The visit argument.
	 * 
	 * @return The visit result.
	 */
	R visitIdentifier(A arg);

	/**
	 * Visits a {@link Operator#HISTORY_CONTEXT} operator.
	 * 
	 * @param arg
	 *        The visit argument.
	 * 
	 * @return The visit result.
	 */
	R visitHistoryContext(A arg);
}
