/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryUtils;

/**
 * Enumeration of operator symbols.
 * 
 * @see Operation
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum Operator {

	/**
	 * The equals operator using binary comparison.
	 */
	EQBINARY(2),
	
	/**
	 * The equals operator using case insensitive comparison.
	 */
	EQCI(2),
	
	/**
	 * The greater than operator.
	 */
	GT(2),
	
	/**
	 * The lower than operator.
	 */
	LT(2),
	
	/**
	 * The greater or equal operator.
	 */
	GE(2),
	
	/**
	 * The lower or equal operator.
	 */
	LE(2),
	
	/**
	 * The boolean and operator.
	 */
	AND(2),
	
	/**
	 * The boolean or operator.
	 */
	OR(2),
	
	/**
	 * The boolean not operator.
	 */
	NOT(1),

	/**
	 * Access to the branch of the wrapped expression.
	 * 
	 * <p>
	 * It is expected that the wrapped expression represents a {@link KnowledgeItem} or its
	 * {@link KnowledgeItem#tId() identifier}.
	 * </p>
	 */
	BRANCH(1),

	/**
	 * Access to the revision of the wrapped expression.
	 * 
	 * <p>
	 * It is expected that the wrapped expression represents a {@link KnowledgeItem} or its
	 * {@link KnowledgeItem#tId() identifier}.
	 * </p>
	 * 
	 * <p>
	 * In contrast to {@link #HISTORY_CONTEXT}, this operator accesses the history context of the
	 * request, when the wrapped expression represents a {@link HistoryUtils#isCurrent(ObjectKey)
	 * current object}.
	 * </p>
	 * 
	 * @see Operator#HISTORY_CONTEXT
	 */
	REVISION(1),

	/**
	 * Access to the history context of the wrapped expression.
	 * 
	 * <p>
	 * It is expected that the wrapped expression represents a {@link KnowledgeItem} or its
	 * {@link KnowledgeItem#tId() identifier}.
	 * </p>
	 * 
	 * <p>
	 * In contrast to {@link #REVISION}, this operator accesses always the
	 * {@link ObjectKey#getHistoryContext() history context} for the item represented by the wrapped
	 * expression.
	 * </p>
	 * 
	 * @see Operator#REVISION
	 */
	HISTORY_CONTEXT(1),

	/**
	 * Access to the type name of the wrapped expression.
	 * 
	 * <p>
	 * It is expected that the wrapped expression represents a {@link KnowledgeItem} or its
	 * {@link KnowledgeItem#tId() identifier}.
	 * </p>
	 */
	TYPE_NAME(1),

	/**
	 * Access to the identifier of the wrapped expression.
	 * 
	 * <p>
	 * It is expected that the wrapped expression represents a {@link KnowledgeItem} or its
	 * {@link KnowledgeItem#tId() identifier}.
	 * </p>
	 */
	IDENTIFIER(1),

	/**
	 * Operator that checks whether a given expression evaluates to <code>null</code>.
	 */
	IS_NULL(1);
	
	
	private final int arguments;

	private Operator(int arguments) {
		this.arguments = arguments;
	}
	
	/**
	 * The name of this operator in textual form.
	 */
	public String getName() {
		return this.name();
	}
	
	/**
	 * Number of arguments this operator expects.
	 */
	public int getExpectedArguments() {
		return arguments;
	}

	/**
	 * Visits this {@link Operator} using the given {@link OperatorVisitor}.
	 * 
	 * @param v
	 *        The visitor that visits this operator.
	 * @param arg
	 *        The visit argument (context)
	 * @return The visit result.
	 */
	public <R, A> R visit(OperatorVisitor<R, A> v, A arg) {
		switch (this) {
			case AND:
				return v.visitAnd(AND, arg);
			case EQBINARY:
				return v.visitEqBinary(EQBINARY, arg);
			case EQCI:
				return v.visitEqCi(EQCI, arg);
			case GE:
				return v.visitGe(GE, arg);
			case GT:
				return v.visitGt(GT, arg);
			case LE:
				return v.visitLe(LE, arg);
			case LT:
				return v.visitLt(LT, arg);
			case OR:
				return v.visitOr(OR, arg);
			case NOT:
				return v.visitNot(NOT, arg);
			case IS_NULL:
				return v.visitIsNull(arg);
			case BRANCH:
				return v.visitBranch(arg);
			case REVISION:
				return v.visitRevision(arg);
			case IDENTIFIER:
				return v.visitIdentifier(arg);
			case HISTORY_CONTEXT:
				return v.visitHistoryContext(arg);
			case TYPE_NAME:
				return v.visitType(arg);
		}
		throw noSuchOperator(this);
	}

	/**
	 * throw to indicate that there is must not be such an operator.
	 */
	public static AssertionError noSuchOperator(Operator operator) throws AssertionError {
		throw new AssertionError("Unexpected operator: " + operator);
	}
	
}
